import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPatientAppointments, cancelAppointment } from '../../api/appointments';
import { submitFeedback, getFeedbackForAppointment } from '../../api/feedback';
import { Card, Button, StatusBadge, Skeleton, Modal, Toast } from '../../components/UI';
import { Calendar, Clock, Star, AlertCircle, Eye, Trash2, Award, ClipboardList } from 'lucide-react';
import { Appointment, DoctorFeedback } from '../../types';

const MyAppointments: React.FC = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'UPCOMING' | 'COMPLETED' | 'CANCELLED'>('UPCOMING');
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Modals state
  const [selectedAppt, setSelectedAppt] = useState<Appointment | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const [feedbackAppt, setFeedbackAppt] = useState<Appointment | null>(null);
  const [rating, setRating] = useState<number>(5);
  const [feedbackText, setFeedbackText] = useState('');
  const [feedbackMap, setFeedbackMap] = useState<Record<string, DoctorFeedback>>({});

  // 1. Fetch appointments
  const { data: appointments = [], isLoading } = useQuery({
    queryKey: ['patientAppointments'],
    queryFn: getPatientAppointments,
  });

  // Fetch feedback records mapping to display ratings for completed appointments
  useEffect(() => {
    const fetchFeedbacks = async () => {
      const completedList = appointments.filter(appt => appt.status === 'COMPLETED');
      const tempMap: Record<string, DoctorFeedback> = {};
      
      for (const appt of completedList) {
        try {
          const res = await getFeedbackForAppointment(appt._id);
          if (res.exists && res.feedback) {
            tempMap[appt._id] = res.feedback;
          }
        } catch (e) {
          // ignore error for single feedback check
        }
      }
      setFeedbackMap(tempMap);
    };

    if (appointments.length > 0) {
      fetchFeedbacks();
    }
  }, [appointments]);

  // Cancel appointment mutation
  const cancelMutation = useMutation({
    mutationFn: cancelAppointment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patientAppointments'] });
      queryClient.invalidateQueries({ queryKey: ['upcomingAppointment'] });
      queryClient.invalidateQueries({ queryKey: ['liveQueueStatus'] });
      setToast({ message: 'Appointment cancelled successfully.', type: 'success' });
      setTimeout(() => setToast(null), 4000);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to cancel appointment.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  // Feedback submission mutation
  const feedbackMutation = useMutation({
    mutationFn: submitFeedback,
    onSuccess: (data) => {
      setFeedbackMap(prev => ({ ...prev, [data.appointmentId]: data }));
      queryClient.invalidateQueries({ queryKey: ['patientAppointments'] });
      setToast({ message: 'Thank you! Feedback submitted successfully.', type: 'success' });
      setTimeout(() => setToast(null), 4000);
      setFeedbackAppt(null);
      setFeedbackText('');
      setRating(5);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to submit feedback.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleCancelClick = (appointmentId: string) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    cancelMutation.mutate(appointmentId);
  };

  const handleFeedbackSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!feedbackAppt) return;
    
    feedbackMutation.mutate({
      doctorId: feedbackAppt.doctorId,
      appointmentId: feedbackAppt._id,
      rating,
      feedback: feedbackText
    });
  };

  const getFilteredAppts = () => {
    return appointments.filter(appt => {
      if (activeTab === 'UPCOMING') return appt.status === 'UPCOMING' || appt.status === 'IN_PROGRESS';
      return appt.status === activeTab;
    });
  };

  const filteredAppts = getFilteredAppts();

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Toast popup */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* Tabs */}
      <div className="flex gap-2 border-b border-slate-100 pb-0.5 select-none overflow-x-auto scrollbar-none">
        {(['UPCOMING', 'COMPLETED', 'CANCELLED'] as const).map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`py-3 px-6 font-extrabold text-sm bg-transparent cursor-pointer border-0 border-b-2 transition-all outline-none leading-none active:scale-98 ${
              activeTab === tab 
                ? 'border-medical-blue-600 text-medical-blue-600' 
                : 'border-transparent text-slate-400 hover:text-slate-650'
            }`}
          >
            {tab === 'UPCOMING' ? 'Upcoming & Active' : tab.replace('_', ' ')}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="210px" className="rounded-3xl" />
          <Skeleton height="210px" className="rounded-3xl" />
        </div>
      ) : filteredAppts.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredAppts.map(appt => (
            <Card key={appt._id} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h4 className="font-extrabold text-sm text-slate-900 leading-snug">Dr. {appt.doctorName}</h4>
                    <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5 block">{appt.department}</span>
                  </div>
                  <StatusBadge status={appt.status} className="flex-shrink-0" />
                </div>

                <div className="grid grid-cols-2 gap-3 mb-4 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-500">
                  <div className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100/50">
                    <Calendar size={14} className="text-medical-blue-500 flex-shrink-0" />
                    <span className="truncate text-slate-700">{appt.date}</span>
                  </div>
                  <div className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100/50">
                    <Clock size={14} className="text-medical-teal-500 flex-shrink-0" />
                    <span className="truncate text-slate-700">{appt.time}</span>
                  </div>
                </div>

                {appt.tokenNumber && (
                  <div className="bg-medical-blue-50/40 border border-medical-blue-100/40 p-2.5 rounded-xl flex items-center justify-between text-xs font-bold mb-4 select-all">
                    <span className="text-slate-450 uppercase text-[9px] tracking-wider">Queue Token</span>
                    <span className="text-medical-blue-650 font-black text-sm">#{appt.tokenNumber}</span>
                  </div>
                )}
              </div>

              <div className="flex flex-col gap-2 mt-4 border-t border-slate-100 pt-4">
                <Button 
                  onClick={() => { setSelectedAppt(appt); setIsDetailsOpen(true); }} 
                  variant="outline" 
                  className="py-2.5 text-xs w-full rounded-xl font-bold shadow-xs flex items-center justify-center gap-1.5"
                >
                  <Eye size={14} />
                  <span>View Details</span>
                </Button>

                {appt.status === 'UPCOMING' && (
                  <div className="flex gap-2">
                    <Button 
                      onClick={() => navigate(`/patient/book?rescheduleId=${appt._id}`)} 
                      variant="outline" 
                      className="py-2.5 text-xs flex-1 rounded-xl font-bold flex items-center justify-center gap-1.5"
                    >
                      <Calendar size={14} />
                      <span>Reschedule</span>
                    </Button>
                    <Button 
                      onClick={() => handleCancelClick(appt._id)} 
                      variant="danger" 
                      className="py-2.5 text-xs flex-1 rounded-xl font-bold flex items-center justify-center gap-1.5"
                    >
                      <Trash2 size={14} />
                      <span>Cancel</span>
                    </Button>
                  </div>
                )}

                {appt.status === 'COMPLETED' && (
                  feedbackMap[appt._id] ? (
                    <div className="flex items-center justify-center gap-1.5 p-2.5 bg-emerald-50/70 border border-emerald-100/60 rounded-xl text-emerald-700 text-xs font-extrabold mt-1 select-none">
                      <Star size={14} fill="currentColor" className="text-emerald-500" />
                      <span>Rated {feedbackMap[appt._id].rating}.0 / 5</span>
                    </div>
                  ) : (
                    <Button 
                      onClick={() => setFeedbackAppt(appt)} 
                      variant="secondary" 
                      className="py-2.5 text-xs mt-1 w-full rounded-xl font-bold flex items-center justify-center gap-1.5"
                    >
                      <Star size={14} />
                      <span>Rate Consultation</span>
                    </Button>
                  )
                )}
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-white border border-slate-100 rounded-3xl select-none shadow-xs max-w-md mx-auto">
          <div className="w-12 h-12 bg-slate-50 border border-slate-100 rounded-2xl flex items-center justify-center text-slate-400 mx-auto mb-4">
            <ClipboardList size={22} />
          </div>
          <p className="text-xs font-extrabold text-slate-400 uppercase tracking-widest">No consultations found</p>
          <p className="text-[10px] text-slate-350 font-bold uppercase tracking-wider mt-1">There are no records in this active category.</p>
        </div>
      )}

      {/* Appointment Details Modal */}
      <Modal 
        isOpen={isDetailsOpen} 
        onClose={() => setIsDetailsOpen(false)} 
        title="Appointment Details"
      >
        {selectedAppt && (
          <div className="space-y-4 text-xs font-bold text-slate-650 select-none">
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Appointment ID</span>
              <span className="text-slate-800 font-mono select-all font-semibold tracking-tight">{selectedAppt._id}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Consulting Doctor</span>
              <span className="text-slate-800 font-extrabold">Dr. {selectedAppt.doctorName}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Department</span>
              <span className="text-slate-800">{selectedAppt.department}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Scheduled Date</span>
              <span className="text-slate-800 font-extrabold">{selectedAppt.date}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Scheduled Time</span>
              <span className="text-slate-800 font-extrabold">{selectedAppt.time}</span>
            </div>
            {selectedAppt.tokenNumber && (
              <div className="flex justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Assigned Token</span>
                <span className="text-medical-blue-650 font-black text-sm">#{selectedAppt.tokenNumber}</span>
              </div>
            )}
            <div className="flex justify-between items-center pb-1">
              <span className="text-slate-400">Status</span>
              <StatusBadge status={selectedAppt.status} />
            </div>

            <Button onClick={() => setIsDetailsOpen(false)} className="w-full mt-6 py-2.5 rounded-xl text-xs font-bold shadow-md">
              Close Details
            </Button>
          </div>
        )}
      </Modal>

      {/* Feedback / Review Modal */}
      <Modal
        isOpen={feedbackAppt !== null}
        onClose={() => setFeedbackAppt(null)}
        title="Consultation Review"
      >
        {feedbackAppt && (
          <form onSubmit={handleFeedbackSubmit} className="space-y-4">
            <p className="text-xs font-semibold text-slate-555 leading-relaxed">
              Please rate your consultation session with Dr. {feedbackAppt.doctorName}. Your feedback helps maintain our secure healthcare network quality.
            </p>
            
            <div className="text-center my-6 bg-slate-50/70 border border-slate-100/50 p-4 rounded-2xl">
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">Rate Experience</label>
              <div className="flex justify-center gap-2 select-none">
                {[1, 2, 3, 4, 5].map(starNum => (
                  <button
                    key={starNum}
                    type="button"
                    onClick={() => setRating(starNum)}
                    className="bg-transparent border-0 cursor-pointer text-amber-400 hover:scale-110 active:scale-95 transition-all outline-none p-1"
                  >
                    <Star size={32} fill={starNum <= rating ? 'currentColor' : 'none'} className="stroke-amber-400 stroke-2" />
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">Review Notes & Remarks</label>
              <textarea
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all outline-none resize-none"
                rows={4}
                placeholder="Share your experience (e.g. prompt checkups, diagnostic clarity, advice)..."
                value={feedbackText}
                onChange={(e) => setFeedbackText(e.target.value)}
                required
              />
            </div>

            <div className="flex gap-3 mt-6">
              <Button type="button" onClick={() => setFeedbackAppt(null)} variant="outline" className="flex-1 py-2.5 text-xs rounded-xl font-bold">
                Cancel
              </Button>
              <Button 
                type="submit" 
                className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-md" 
                loading={feedbackMutation.isPending}
              >
                Submit Review
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};

export default MyAppointments;

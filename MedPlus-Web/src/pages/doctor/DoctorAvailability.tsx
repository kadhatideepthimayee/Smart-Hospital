import React, { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getDoctorProfile, updateAvailability } from '../../api/doctors';
import { Card, Button, Skeleton, Toast } from '../../components/UI';
import { Clock, Calendar, DollarSign } from 'lucide-react';
import { DoctorProfile as IDoctorProfile } from '../../types';

const DAYS_OF_WEEK = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const DoctorAvailability: React.FC = () => {
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Form states matching Availability settings
  const [workingDays, setWorkingDays] = useState<string[]>([]);
  const [consultationStartTime, setConsultationStartTime] = useState('09:00 AM');
  const [consultationEndTime, setConsultationEndTime] = useState('05:00 PM');
  const [lunchStartTime, setLunchStartTime] = useState('01:00 PM');
  const [lunchEndTime, setLunchEndTime] = useState('02:00 PM');
  const [breakStartTime, setBreakStartTime] = useState('');
  const [breakEndTime, setBreakEndTime] = useState('');
  const [slotDuration, setSlotDuration] = useState<number>(15);
  const [consultationFee, setConsultationFee] = useState<number>(15);

  // 1. Fetch Doctor Profile
  const { data: profile, isLoading } = useQuery({
    queryKey: ['doctorProfile'],
    queryFn: getDoctorProfile,
  });

  // Sync state with fetched profile details
  useEffect(() => {
    if (profile) {
      setWorkingDays(profile.workingDays || []);
      setConsultationStartTime(profile.consultationStartTime || '09:00 AM');
      setConsultationEndTime(profile.consultationEndTime || '05:00 PM');
      setLunchStartTime(profile.lunchStartTime || '01:00 PM');
      setLunchEndTime(profile.lunchEndTime || '02:00 PM');
      setBreakStartTime(profile.breakStartTime || '');
      setBreakEndTime(profile.breakEndTime || '');
      setSlotDuration(profile.slotDuration || 15);
      setConsultationFee(profile.consultationFee || 15);
    }
  }, [profile]);

  // Mutation to update availability details
  const availabilityMutation = useMutation({
    mutationFn: updateAvailability,
    onSuccess: (updatedProfile) => {
      queryClient.setQueryData(['doctorProfile'], updatedProfile);
      queryClient.invalidateQueries({ queryKey: ['doctorProfile'] });
      setToast({ message: 'Availability and slot settings updated successfully.', type: 'success' });
      setTimeout(() => setToast(null), 4000);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to update availability schedule.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleCheckboxChange = (day: string) => {
    setWorkingDays(prev => 
      prev.includes(day) ? prev.filter(d => d !== day) : [...prev, day]
    );
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (workingDays.length === 0) {
      setToast({ message: 'Please select at least one working day.', type: 'error' });
      return;
    }

    availabilityMutation.mutate({
      workingDays,
      consultationStartTime,
      consultationEndTime,
      lunchStartTime,
      lunchEndTime,
      breakStartTime,
      breakEndTime,
      slotDuration: Number(slotDuration),
      consultationFee: Number(consultationFee)
    });
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-in fade-in duration-300">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {isLoading ? (
        <Card className="rounded-3xl p-8" hoverEffect={false}>
          <Skeleton height="350px" className="rounded-2xl" />
        </Card>
      ) : (
        <form onSubmit={handleSave} className="space-y-6">
          
          {/* Working Days */}
          <Card className="p-6 rounded-3xl" hoverEffect={false}>
            <h3 className="text-sm font-bold text-slate-800 tracking-tight mb-5 border-b border-slate-100 pb-3.5 flex items-center gap-2 select-none">
              <Calendar size={18} className="text-medical-blue-600 flex-shrink-0" /> 
              <span>1. Selected Consultation Days</span>
            </h3>
            
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-2">
              {DAYS_OF_WEEK.map(day => (
                <label 
                  key={day} 
                  className={`flex items-center gap-3 p-3.5 border rounded-2xl cursor-pointer hover:bg-slate-50 transition-all select-none duration-150 active:scale-97 ${
                    workingDays.includes(day) 
                      ? 'border-medical-blue-600 bg-medical-blue-50/20 text-medical-blue-700 font-extrabold shadow-xs shadow-medical-blue-500/5' 
                      : 'border-slate-100 bg-white text-slate-600'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={workingDays.includes(day)}
                    onChange={() => handleCheckboxChange(day)}
                    className="accent-medical-blue-600 w-4 h-4 cursor-pointer"
                  />
                  <span className="text-xs leading-none">{day}</span>
                </label>
              ))}
            </div>
          </Card>

          {/* Consultation Hours */}
          <Card className="p-6 rounded-3xl" hoverEffect={false}>
            <h3 className="text-sm font-bold text-slate-800 tracking-tight mb-5 border-b border-slate-100 pb-3.5 flex items-center gap-2 select-none">
              <Clock size={18} className="text-medical-blue-600 flex-shrink-0" /> 
              <span>2. Consultation Hours & Breaks</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Clinic Opens (Start Time)</label>
                <input
                  type="text"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  placeholder="e.g. 09:00 AM"
                  value={consultationStartTime}
                  onChange={(e) => setConsultationStartTime(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Clinic Closes (End Time)</label>
                <input
                  type="text"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  placeholder="e.g. 05:00 PM"
                  value={consultationEndTime}
                  onChange={(e) => setConsultationEndTime(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Lunch Interval Starts</label>
                <input
                  type="text"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  placeholder="e.g. 01:00 PM"
                  value={lunchStartTime}
                  onChange={(e) => setLunchStartTime(e.target.value)}
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Lunch Interval Ends</label>
                <input
                  type="text"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  placeholder="e.g. 02:00 PM"
                  value={lunchEndTime}
                  onChange={(e) => setLunchEndTime(e.target.value)}
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Other Break Starts (Optional)</label>
                <input
                  type="text"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  placeholder="e.g. 03:30 PM"
                  value={breakStartTime}
                  onChange={(e) => setBreakStartTime(e.target.value)}
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Other Break Ends (Optional)</label>
                <input
                  type="text"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  placeholder="e.g. 04:00 PM"
                  value={breakEndTime}
                  onChange={(e) => setBreakEndTime(e.target.value)}
                />
              </div>
            </div>
          </Card>

          {/* Fee & Slot Duration */}
          <Card className="p-6 rounded-3xl" hoverEffect={false}>
            <h3 className="text-sm font-bold text-slate-800 tracking-tight mb-5 border-b border-slate-100 pb-3.5 flex items-center gap-2 select-none">
              <DollarSign size={18} className="text-medical-blue-600 flex-shrink-0" /> 
              <span>3. Consultation Fee & Slot Interval</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Consultation Slot Duration</label>
                <select
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  value={slotDuration}
                  onChange={(e) => setSlotDuration(Number(e.target.value))}
                >
                  <option value={15}>15 Minutes</option>
                  <option value={30}>30 Minutes</option>
                  <option value={60}>60 Minutes</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Consultation Fee ($)</label>
                <input
                  type="number"
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                  min={0}
                  step={1}
                  value={consultationFee}
                  onChange={(e) => setConsultationFee(Number(e.target.value))}
                  required
                />
              </div>
            </div>
          </Card>

          <div className="text-right">
            <Button 
              type="submit" 
              className="py-2.5 px-8 text-xs rounded-xl font-bold shadow-md hover:-translate-y-0.5 active:translate-y-0" 
              loading={availabilityMutation.isPending}
            >
              Save Availability Setup
            </Button>
          </div>
        </form>
      )}
    </div>
  );
};

export default DoctorAvailability;

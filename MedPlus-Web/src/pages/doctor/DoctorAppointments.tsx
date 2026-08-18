import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getDoctorAppointments } from '../../api/appointments';
import { Card, Button, StatusBadge, Skeleton, Modal } from '../../components/UI';
import { formatDateToBackend } from '../../lib/utils';
import { Calendar, Clock, Eye, AlertCircle, ClipboardList } from 'lucide-react';
import { Appointment } from '../../types';

const DoctorAppointments: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'TODAY' | 'UPCOMING' | 'COMPLETED'>('TODAY');
  const [selectedAppt, setSelectedAppt] = useState<Appointment | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  // Fetch Doctor's appointments
  const { data: appointments = [], isLoading, error } = useQuery({
    queryKey: ['doctorAppointments'],
    queryFn: getDoctorAppointments,
  });

  const getFilteredAppts = () => {
    const todayStr = formatDateToBackend(new Date());
    
    return appointments.filter(appt => {
      if (activeTab === 'TODAY') {
        return appt.date === todayStr && appt.status !== 'COMPLETED' && appt.status !== 'CANCELLED';
      }
      if (activeTab === 'UPCOMING') {
        return appt.date !== todayStr && appt.status !== 'COMPLETED' && appt.status !== 'CANCELLED';
      }
      return appt.status === activeTab;
    });
  };

  const filteredAppts = getFilteredAppts();

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Tabs */}
      <div className="flex gap-2 border-b border-slate-100 pb-0.5 overflow-x-auto select-none scrollbar-none">
        {(['TODAY', 'UPCOMING', 'COMPLETED'] as const).map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`py-3 px-6 font-extrabold text-sm bg-transparent cursor-pointer border-0 border-b-2 transition-all outline-none leading-none active:scale-98 ${
              activeTab === tab 
                ? 'border-medical-blue-600 text-medical-blue-600' 
                : 'text-slate-400 hover:text-slate-650 border-transparent'
            }`}
          >
            {tab === 'TODAY' ? "Today's Ledger" : tab.replace('_', ' ')}
          </button>
        ))}
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="170px" className="rounded-3xl" />
          <Skeleton height="170px" className="rounded-3xl" />
        </div>
      ) : error ? (
        <div className="bg-rose-50 border border-rose-100 p-4.5 rounded-2xl flex items-center gap-3 text-xs text-rose-700 font-semibold max-w-md mx-auto">
          <AlertCircle size={18} className="text-rose-600 flex-shrink-0" />
          <span>Failed to load appointments log. Please try again.</span>
        </div>
      ) : filteredAppts.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredAppts.map(appt => (
            <Card key={appt._id} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h4 className="font-extrabold text-sm text-slate-805 leading-snug">Patient: {appt.patientName}</h4>
                    {appt.tokenNumber && (
                      <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5 block">Queue Token #{appt.tokenNumber}</span>
                    )}
                  </div>
                  <StatusBadge status={appt.status} className="flex-shrink-0 shadow-xs" />
                </div>

                <div className="grid grid-cols-2 gap-3.5 mb-4 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-500">
                  <div className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100/50">
                    <Calendar size={14} className="text-medical-blue-500 flex-shrink-0" />
                    <span className="truncate text-slate-700">{appt.date}</span>
                  </div>
                  <div className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100/50">
                    <Clock size={14} className="text-medical-teal-500 flex-shrink-0" />
                    <span className="truncate text-slate-700">{appt.time}</span>
                  </div>
                </div>
              </div>

              <Button 
                onClick={() => { setSelectedAppt(appt); setIsDetailsOpen(true); }} 
                variant="outline" 
                className="py-2.5 text-xs w-full rounded-xl mt-3 font-bold shadow-xs flex items-center justify-center gap-1.5"
              >
                <Eye size={14} />
                <span>View Details</span>
              </Button>
            </Card>
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-white border border-slate-100 rounded-3xl select-none max-w-md mx-auto shadow-xs p-8">
          <div className="w-12 h-12 bg-slate-50 border border-slate-100 rounded-2xl flex items-center justify-center text-slate-400 mx-auto mb-4">
            <ClipboardList size={22} />
          </div>
          <p className="text-xs font-extrabold text-slate-400 uppercase tracking-widest">No consultations in this section</p>
          <p className="text-[10px] text-slate-350 font-bold uppercase tracking-wider mt-1">There are no matching schedules registered.</p>
        </div>
      )}

      {/* Details Modal */}
      <Modal 
        isOpen={isDetailsOpen} 
        onClose={() => setIsDetailsOpen(false)} 
        title="Consultation Record Details"
      >
        {selectedAppt && (
          <div className="space-y-4 text-xs font-bold text-slate-650 select-none">
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Appointment ID</span>
              <span className="text-slate-800 font-mono select-all font-semibold tracking-tight">{selectedAppt._id}</span>
            </div>
            <div className="flex justify-between border-b border-slate-100 pb-2.5">
              <span className="text-slate-400">Patient Name</span>
              <span className="text-slate-800 font-extrabold">{selectedAppt.patientName}</span>
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
                <span className="text-slate-400">Queue Token</span>
                <span className="text-medical-blue-650 font-black text-sm">#{selectedAppt.tokenNumber}</span>
              </div>
            )}
            <div className="flex justify-between items-center pb-1">
              <span className="text-slate-400">Current Status</span>
              <StatusBadge status={selectedAppt.status} />
            </div>

            <Button onClick={() => setIsDetailsOpen(false)} className="w-full mt-6 py-2.5 rounded-xl text-xs font-bold shadow-md">
              Close Details
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default DoctorAppointments;

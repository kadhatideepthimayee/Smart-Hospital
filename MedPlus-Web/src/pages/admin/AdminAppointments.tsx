import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAdminAppointments } from '../../api/admin';
import { Card, Skeleton, StatusBadge } from '../../components/UI';
import { Search, Calendar, User, Clock, Stethoscope } from 'lucide-react';

const AdminAppointments: React.FC = () => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'UPCOMING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'>('ALL');

  // Fetch all appointments
  const { data: appointments = [], isLoading } = useQuery({
    queryKey: ['adminAllAppointments'],
    queryFn: getAdminAppointments,
  });

  const filteredAppointments = appointments.filter(appt => {
    const matchesSearch =
      (appt.patientName && appt.patientName.toLowerCase().includes(search.toLowerCase())) ||
      (appt.doctorName && appt.doctorName.toLowerCase().includes(search.toLowerCase())) ||
      (appt.department && appt.department.toLowerCase().includes(search.toLowerCase()));

    if (statusFilter === 'ALL') return matchesSearch;
    return matchesSearch && appt.status?.toUpperCase() === statusFilter;
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center select-none border-b border-slate-100 pb-5">
        <div>
          <h3 className="text-lg font-extrabold text-slate-900 tracking-tight">Clinic Appointments</h3>
          <p className="text-xs font-bold text-slate-400 mt-1">
            Browse and monitor all appointment logs and slot allocations across the clinic.
          </p>
        </div>

        <div className="flex gap-2.5 w-full sm:w-auto">
          <select
            className="px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all cursor-pointer bg-white"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as any)}
          >
            <option value="ALL">All Statuses</option>
            <option value="UPCOMING">Upcoming</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          <div className="relative w-full sm:w-64">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
              <Search size={14} />
            </div>
            <input
              type="text"
              className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
              placeholder="Search patient, doctor, specialty..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Skeleton height="150px" className="rounded-3xl" />
          <Skeleton height="150px" className="rounded-3xl" />
          <Skeleton height="150px" className="rounded-3xl" />
        </div>
      ) : filteredAppointments.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredAppointments.map(appt => (
            <Card key={appt._id} className="p-6 rounded-3xl flex flex-col justify-between" hoverEffect={true}>
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3 select-none">
                    <div className="w-10 h-10 rounded-xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-xs shadow-xs">
                      #{appt.tokenNumber || 'T'}
                    </div>
                    <div>
                      <h4 className="font-extrabold text-sm text-slate-900 leading-snug">Patient: {appt.patientName || 'N/A'}</h4>
                      <span className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest block mt-0.5">Token Appointment</span>
                    </div>
                  </div>
                  <StatusBadge status={appt.status} className="shadow-xs flex-shrink-0" />
                </div>

                <div className="flex flex-col gap-2 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-550">
                  <div className="flex items-center justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400 flex items-center gap-1.5"><Stethoscope size={12} /> Practitioner</span>
                    <span className="text-slate-800 font-bold">Dr. {appt.doctorName || 'N/A'}</span>
                  </div>
                  <div className="flex items-center justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400 flex items-center gap-1.5"><Calendar size={12} /> Date</span>
                    <span className="text-slate-800 font-bold font-mono">{appt.date}</span>
                  </div>
                  <div className="flex items-center justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400 flex items-center gap-1.5"><Clock size={12} /> Time Slot</span>
                    <span className="text-slate-800 font-bold font-mono">{appt.time}</span>
                  </div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-12 h-12 bg-slate-50 border border-slate-100 rounded-2xl flex items-center justify-center text-slate-400 mx-auto mb-4">
            <Calendar size={20} />
          </div>
          <p className="text-xs font-extrabold text-slate-400 uppercase tracking-widest">No appointments found</p>
          <p className="text-[10px] text-slate-350 font-bold uppercase tracking-wider mt-1">There are no appointments registered under this filter.</p>
        </Card>
      )}
    </div>
  );
};

export default AdminAppointments;

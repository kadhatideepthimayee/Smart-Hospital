import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAdminPatients } from '../../api/admin';
import { Card, Skeleton, StatusBadge } from '../../components/UI';
import { Search, Users, Phone, Mail } from 'lucide-react';

const AdminPatients: React.FC = () => {
  const [search, setSearch] = useState('');

  // Fetch all patient profiles
  const { data: patients = [], isLoading } = useQuery({
    queryKey: ['adminAllPatients'],
    queryFn: getAdminPatients,
  });

  const filteredPatients = patients.filter(patient => {
    return (
      patient.fullName.toLowerCase().includes(search.toLowerCase()) ||
      patient.email.toLowerCase().includes(search.toLowerCase()) ||
      (patient.phone && patient.phone.includes(search))
    );
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center select-none border-b border-slate-100 pb-5">
        <div>
          <h3 className="text-lg font-extrabold text-slate-900 tracking-tight">Patient Directory</h3>
          <p className="text-xs font-bold text-slate-400 mt-1">
            Browse and monitor profiles of all registered patients in the system.
          </p>
        </div>

        <div className="relative w-full sm:w-64">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            <Search size={14} />
          </div>
          <input
            type="text"
            className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
            placeholder="Search patient by name, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Skeleton height="140px" className="rounded-3xl" />
          <Skeleton height="140px" className="rounded-3xl" />
          <Skeleton height="140px" className="rounded-3xl" />
        </div>
      ) : filteredPatients.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredPatients.map(patient => (
            <Card key={patient.uid || patient._id} className="p-6 rounded-3xl flex flex-col justify-between" hoverEffect={true}>
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3 select-none">
                    <div className="w-10 h-10 rounded-xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-sm uppercase shadow-xs">
                      {patient.fullName.substring(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <h4 className="font-extrabold text-sm text-slate-900 leading-snug">{patient.fullName}</h4>
                      <span className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest block mt-0.5">Patient Account</span>
                    </div>
                  </div>
                  <StatusBadge status={patient.status || 'ACTIVE'} className="shadow-xs flex-shrink-0" />
                </div>

                <div className="flex flex-col gap-2 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-550">
                  <div className="flex items-center gap-2 bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <Mail size={12} className="text-slate-400" />
                    <span className="text-slate-800 select-all font-mono text-[10px] truncate w-full">{patient.email}</span>
                  </div>
                  <div className="flex items-center gap-2 bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <Phone size={12} className="text-slate-400" />
                    <span className="text-slate-800 font-bold select-all">{patient.phone || 'N/A'}</span>
                  </div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-12 h-12 bg-slate-50 border border-slate-100 rounded-2xl flex items-center justify-center text-slate-400 mx-auto mb-4">
            <Users size={20} />
          </div>
          <p className="text-xs font-extrabold text-slate-400 uppercase tracking-widest">No patients found</p>
          <p className="text-[10px] text-slate-350 font-bold uppercase tracking-wider mt-1">There are no patients registered matching the query.</p>
        </Card>
      )}
    </div>
  );
};

export default AdminPatients;

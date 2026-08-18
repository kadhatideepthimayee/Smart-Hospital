import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getAllDoctors } from '../../api/admin';
import { Card, Skeleton, StatusBadge } from '../../components/UI';
import { Search, UserCheck } from 'lucide-react';

const AdminDoctors: React.FC = () => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'VERIFIED' | 'PENDING' | 'REJECTED'>('ALL');

  // Fetch all doctor profiles
  const { data: doctors = [], isLoading } = useQuery({
    queryKey: ['adminAllDoctors'],
    queryFn: getAllDoctors,
  });

  const filteredDoctors = doctors.filter(doc => {
    const matchesSearch = doc.fullName.toLowerCase().includes(search.toLowerCase()) || 
                          (doc.specialization && doc.specialization.toLowerCase().includes(search.toLowerCase()));
    
    if (statusFilter === 'ALL') return matchesSearch;
    return matchesSearch && doc.verificationStatus?.toUpperCase() === statusFilter;
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center select-none border-b border-slate-100 pb-5">
        <p className="text-xs font-bold text-slate-400">
          Browse and monitor credentials of all registered practitioners.
        </p>
        
        <div className="flex gap-2.5 w-full sm:w-auto">
          <select 
            className="px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all cursor-pointer bg-white"
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value as any)}
          >
            <option value="ALL">All Statuses</option>
            <option value="VERIFIED">Verified</option>
            <option value="PENDING">Pending</option>
            <option value="REJECTED">Rejected</option>
          </select>

          <div className="relative w-full sm:w-64">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
              <Search size={14} />
            </div>
            <input
              type="text"
              className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-slate-200 text-xs font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
              placeholder="Search doctor or specialty..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="170px" className="rounded-3xl" />
          <Skeleton height="170px" className="rounded-3xl" />
        </div>
      ) : filteredDoctors.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredDoctors.map(doc => (
            <Card key={doc.uid} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3.5 select-none">
                    <div className="w-12 h-12 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-sm uppercase shadow-xs">
                      {doc.fullName.substring(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <h4 className="font-extrabold text-sm text-slate-900 leading-snug">Dr. {doc.fullName}</h4>
                      <span className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest block mt-0.5">{doc.specialization || 'Clinical'} specialist</span>
                    </div>
                  </div>
                  <StatusBadge status={doc.verificationStatus} className="shadow-xs flex-shrink-0" />
                </div>

                <div className="flex flex-col gap-2.5 mb-2 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-550">
                  {doc.email && (
                    <div className="flex justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                      <span className="text-slate-400">Email Address</span>
                      <span className="text-slate-800 select-all font-mono text-[10px]">{doc.email}</span>
                    </div>
                  )}
                  <div className="flex justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400">Qualification</span>
                    <span className="text-slate-800 font-bold">{doc.qualification || 'MBBS'}</span>
                  </div>
                  <div className="flex justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400">Consultation Fee</span>
                    <span className="text-medical-teal-650 font-black">${doc.consultationFee || 15}</span>
                  </div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-12 h-12 bg-slate-50 border border-slate-100 rounded-2xl flex items-center justify-center text-slate-400 mx-auto mb-4">
            <UserCheck size={20} />
          </div>
          <p className="text-xs font-extrabold text-slate-400 uppercase tracking-widest">No practitioners matching query</p>
          <p className="text-[10px] text-slate-350 font-bold uppercase tracking-wider mt-1">There are no practitioners registered under this filter.</p>
        </Card>
      )}
    </div>
  );
};

export default AdminDoctors;

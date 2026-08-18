import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAllDoctors, verifyDoctor } from '../../api/admin';
import { Card, Button, StatusBadge, Skeleton, Toast } from '../../components/UI';
import { Shield, ShieldAlert, ShieldCheck, Users, CheckCircle, XCircle, BarChart as ChartIcon } from 'lucide-react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  Tooltip, 
  ResponsiveContainer, 
  PieChart, 
  Pie, 
  Cell 
} from 'recharts';

const COLORS = ['#2563eb', '#10b981', '#f59e0b', '#ef4444'];

const AdminDashboard: React.FC = () => {
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Fetch all doctor profiles
  const { data: doctors = [], isLoading } = useQuery({
    queryKey: ['adminAllDoctors'],
    queryFn: getAllDoctors,
  });

  // Verify doctor profile mutation
  const verifyMutation = useMutation({
    mutationFn: ({ doctorId, status, reason }: { doctorId: string; status: 'VERIFIED' | 'APPROVED' | 'REJECTED'; reason?: string }) =>
      verifyDoctor(doctorId, status, reason),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['adminAllDoctors'] });
      setToast({ message: `Doctor profile verification set to ${variables.status.toLowerCase()}.`, type: 'success' });
      setTimeout(() => setToast(null), 3000);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Verification update failed.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleVerifyClick = (doctorId: string, status: 'VERIFIED' | 'REJECTED', reason = '') => {
    verifyMutation.mutate({ doctorId, status, reason });
  };

  const pendingList = doctors.filter(doc => doc.verificationStatus?.toUpperCase() === 'PENDING');
  const verifiedCount = doctors.filter(doc => doc.verificationStatus?.toUpperCase() === 'VERIFIED' || doc.verificationStatus?.toUpperCase() === 'APPROVED').length;
  const rejectedCount = doctors.filter(doc => doc.verificationStatus?.toUpperCase() === 'REJECTED').length;

  // 1. Chart Data: Specialization Distribution
  const specData = Object.entries(
    doctors.reduce<Record<string, number>>((acc, curr) => {
      const spec = curr.specialization || 'General Care';
      acc[spec] = (acc[spec] || 0) + 1;
      return acc;
    }, {})
  ).map(([name, count]) => ({ name, count }));

  // 2. Chart Data: Status Pie Distribution
  const pieData = [
    { name: 'Verified', value: verifiedCount },
    { name: 'Pending', value: pendingList.length },
    { name: 'Rejected', value: rejectedCount }
  ].filter(d => d.value > 0);

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Toast popup */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-medical-blue-600 to-medical-blue-500 text-white rounded-3xl p-6 md:p-8 flex justify-between items-center shadow-lg shadow-medical-blue-500/10 select-none">
        <div>
          <h1 className="text-xl md:text-2xl font-black mb-1.5 tracking-tight">Control Console</h1>
          <p className="text-white/85 text-xs font-semibold leading-relaxed max-w-xl">Review doctor profiles, verify clinic credentials, and monitor system workload.</p>
        </div>
        <Shield size={44} className="opacity-20 hidden sm:block flex-shrink-0" />
      </div>

      {/* Overview stats */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          <Skeleton height="110px" className="rounded-3xl" />
          <Skeleton height="110px" className="rounded-3xl" />
          <Skeleton height="110px" className="rounded-3xl" />
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 select-none">
          <div className="bg-white border border-slate-100/75 p-6 rounded-3xl flex items-center justify-between shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05)] hover:shadow-md transition-all duration-300">
            <div>
              <span className="text-[10px] text-slate-400 font-extrabold block mb-1.5 uppercase tracking-widest">PENDING VERIFICATION</span>
              <p className="text-2xl font-black text-amber-600">{pendingList.length} Doctors</p>
            </div>
            <div className="p-3 bg-amber-50/70 border border-amber-100/50 text-amber-600 rounded-2xl shadow-xs">
              <ShieldAlert size={22} />
            </div>
          </div>

          <div className="bg-white border border-slate-100/75 p-6 rounded-3xl flex items-center justify-between shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05)] hover:shadow-md transition-all duration-300">
            <div>
              <span className="text-[10px] text-slate-400 font-extrabold block mb-1.5 uppercase tracking-widest">VERIFIED DOCTORS</span>
              <p className="text-2xl font-black text-emerald-600">{verifiedCount} Profiles</p>
            </div>
            <div className="p-3 bg-emerald-50/70 border border-emerald-100/50 text-emerald-600 rounded-2xl shadow-xs">
              <ShieldCheck size={22} />
            </div>
          </div>

          <div className="bg-white border border-slate-100/75 p-6 rounded-3xl flex items-center justify-between shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05)] hover:shadow-md transition-all duration-300">
            <div>
              <span className="text-[10px] text-slate-400 font-extrabold block mb-1.5 uppercase tracking-widest">TOTAL DOCTORS</span>
              <p className="text-2xl font-black text-slate-900">{doctors.length} Registered</p>
            </div>
            <div className="p-3 bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 rounded-2xl shadow-xs">
              <Users size={22} />
            </div>
          </div>
        </div>
      )}

      {/* Analytics Charts Row */}
      {!isLoading && doctors.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 select-none">
          {/* Department Specializations Bar Chart */}
          <Card className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={false}>
            <h3 className="text-xs font-bold text-slate-700 uppercase tracking-widest mb-6 flex items-center gap-1.5 border-b border-slate-100 pb-3">
              <ChartIcon size={14} className="text-medical-blue-600 flex-shrink-0" />
              <span>Specialists by Department</span>
            </h3>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={specData} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
                  <XAxis dataKey="name" stroke="#94a3b8" fontSize={9} tickLine={false} />
                  <YAxis stroke="#94a3b8" fontSize={9} tickLine={false} allowDecimals={false} />
                  <Tooltip 
                    contentStyle={{ background: '#ffffff', border: '1px solid #f1f5f9', borderRadius: '16px', fontSize: '11px', fontWeight: '600', boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}
                  />
                  <Bar dataKey="count" fill="#2563eb" radius={[6, 6, 0, 0]} maxBarSize={28} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>

          {/* Verification Status Pie Chart */}
          <Card className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={false}>
            <h3 className="text-xs font-bold text-slate-700 uppercase tracking-widest mb-6 flex items-center gap-1.5 border-b border-slate-100 pb-3">
              <ShieldCheck size={14} className="text-medical-teal-600 flex-shrink-0" />
              <span>Verification Status breakdown</span>
            </h3>
            <div className="h-64 w-full flex items-center justify-center">
              {pieData.length > 0 ? (
                <div className="w-full h-full flex flex-col sm:flex-row items-center justify-around">
                  <div className="w-40 h-40">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={pieData}
                          cx="50%"
                          cy="50%"
                          innerRadius={48}
                          outerRadius={68}
                          paddingAngle={3}
                          dataKey="value"
                        >
                          {pieData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                  <div className="flex flex-col gap-2.5 text-xs font-extrabold text-slate-600 mt-4 sm:mt-0">
                    {pieData.map((d, index) => (
                      <div key={d.name} className="flex items-center gap-2 bg-slate-50 p-2 px-3 rounded-xl border border-slate-100/50">
                        <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: COLORS[index % COLORS.length] }}></span>
                        <span>{d.name}: {d.value}</span>
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <span className="text-xs font-semibold text-slate-400">No chart data available.</span>
              )}
            </div>
          </Card>
        </div>
      )}

      {/* Verification requests list */}
      <div className="space-y-4">
        <h3 className="text-base font-extrabold text-slate-805 tracking-tight select-none">
          Pending Doctor Verification Requests
        </h3>
        
        {isLoading ? (
          <Skeleton height="110px" className="rounded-2xl" />
        ) : pendingList.length > 0 ? (
          <div className="space-y-3.5">
            {pendingList.map(doc => (
              <Card key={doc.uid} className="flex flex-col sm:flex-row justify-between sm:items-center gap-4 p-5 rounded-3xl" hoverEffect={true}>
                <div className="flex items-center gap-4 select-none">
                  <div className="w-12 h-12 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-sm uppercase shadow-xs">
                    {doc.fullName.substring(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <h4 className="font-extrabold text-sm text-slate-900 leading-snug">Dr. {doc.fullName}</h4>
                    <p className="text-[10px] text-slate-400 font-extrabold uppercase tracking-wider mt-1">{doc.specialization} &bull; {doc.qualification || 'MBBS'}</p>
                  </div>
                </div>

                <div className="flex gap-2.5 select-none">
                  <Button 
                    onClick={() => handleVerifyClick(doc.uid, 'VERIFIED')} 
                    className="py-2 px-4 text-xs bg-emerald-600 hover:bg-emerald-700 active:bg-emerald-800 text-white border-0 shadow-sm shadow-emerald-500/5 rounded-xl font-bold hover:-translate-y-0.5 active:translate-y-0"
                  >
                    <CheckCircle size={14} />
                    <span>Approve</span>
                  </Button>
                  <Button 
                    onClick={() => {
                      const reason = window.prompt('Enter rejection reason:');
                      if (reason !== null) handleVerifyClick(doc.uid, 'REJECTED', reason);
                    }} 
                    className="py-2 px-4 text-xs bg-rose-600 hover:bg-rose-700 active:bg-rose-800 text-white border-0 shadow-sm shadow-rose-500/5 rounded-xl font-bold hover:-translate-y-0.5 active:translate-y-0"
                  >
                    <XCircle size={14} />
                    <span>Reject</span>
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <Card className="text-center py-16 text-xs font-semibold text-slate-400 border border-dashed border-slate-200 rounded-3xl bg-slate-50/20 select-none">
            All doctor profiles have been reviewed and verified.
          </Card>
        )}
      </div>

    </div>
  );
};

export default AdminDashboard;

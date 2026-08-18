import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getDoctorPatients } from '../../api/doctors';
import { getDoctorAppointments } from '../../api/appointments';
import { Card, Button, Skeleton, Modal, Toast, StatusBadge } from '../../components/UI';
import { User, Mail, Phone, Calendar, Search, AlertCircle, Users } from 'lucide-react';
import { User as IUser, Appointment } from '../../types';

const DoctorPatients: React.FC = () => {
  const [search, setSearch] = useState('');
  const [selectedPatient, setSelectedPatient] = useState<IUser | null>(null);
  const [history, setHistory] = useState<Appointment[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // 1. Fetch patients
  const { data: patients = [], isLoading, error } = useQuery({
    queryKey: ['doctorPatients'],
    queryFn: getDoctorPatients,
  });

  const handleViewHistory = async (patient: IUser) => {
    setSelectedPatient(patient);
    setLoadingHistory(true);
    try {
      // Fetch appointment logs and filter by this patient
      const allAppts = await getDoctorAppointments();
      const filtered = allAppts.filter(appt => appt.patientId === patient.uid);
      setHistory(filtered);
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to fetch consultation records.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    } finally {
      setLoadingHistory(false);
    }
  };

  const filteredPatients = patients.filter(pat => 
    pat.fullName.toLowerCase().includes(search.toLowerCase()) ||
    (pat.email && pat.email.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      <div className="flex flex-col sm:flex-row gap-4 justify-between items-center select-none border-b border-slate-100 pb-5">
        <p className="text-xs font-bold text-slate-400">
          Browse records of patients who have consulted with you.
        </p>
        <div className="relative w-full sm:w-80">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            <Search size={16} />
          </div>
          <input
            type="text"
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
            placeholder="Search by name or email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="160px" className="rounded-3xl" />
          <Skeleton height="160px" className="rounded-3xl" />
        </div>
      ) : error ? (
        <div className="bg-rose-50 border border-rose-100 p-4.5 rounded-2xl flex items-center gap-3 text-xs text-rose-700 font-semibold max-w-md mx-auto">
          <AlertCircle size={18} className="text-rose-600 flex-shrink-0" />
          <span>Failed to load patient records. Please refresh the page.</span>
        </div>
      ) : filteredPatients.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {filteredPatients.map(pat => (
            <Card key={pat.uid} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div>
                <div className="flex items-center gap-3.5 mb-4 select-none">
                  <div className="w-12 h-12 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-sm uppercase shadow-xs">
                    {pat.fullName.substring(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <h4 className="font-extrabold text-sm text-slate-900 leading-snug">{pat.fullName}</h4>
                    <span className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest block mt-0.5">PATIENT PROFILE</span>
                  </div>
                </div>

                <div className="flex flex-col gap-2 mb-4 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-500">
                  {pat.email && (
                    <div className="flex items-center gap-2">
                      <Mail size={14} className="text-slate-400 flex-shrink-0" />
                      <span className="text-slate-700 truncate">{pat.email}</span>
                    </div>
                  )}
                  {pat.phone && (
                    <div className="flex items-center gap-2">
                      <Phone size={14} className="text-slate-400 flex-shrink-0" />
                      <span className="text-slate-700 truncate">{pat.phone}</span>
                    </div>
                  )}
                </div>
              </div>

              <Button 
                onClick={() => handleViewHistory(pat)} 
                variant="outline" 
                className="py-2.5 text-xs w-full rounded-xl mt-3 font-bold shadow-xs flex items-center justify-center gap-1.5"
              >
                <span>View Consultation History</span>
              </Button>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-14 h-14 bg-slate-50 text-slate-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-slate-100/70 shadow-xs">
            <Users size={24} />
          </div>
          <h3 className="text-base font-extrabold text-slate-800">No Patients Found</h3>
          <p className="text-xs text-slate-450 font-semibold mt-2 leading-relaxed">Patients consulting with you will display here.</p>
        </Card>
      )}

      {/* Patient History Modal */}
      <Modal
        isOpen={selectedPatient !== null}
        onClose={() => setSelectedPatient(null)}
        title="Consultation History Log"
      >
        {selectedPatient && (
          <div className="space-y-4 text-xs font-bold text-slate-655 select-none">
            <div className="border-b border-slate-100 pb-4 mb-2">
              <h4 className="font-black text-base text-slate-900">{selectedPatient.fullName}</h4>
              <p className="text-xs text-slate-450 mt-1 font-semibold leading-relaxed">
                Email: {selectedPatient.email} <span className="text-slate-300 mx-1.5">|</span> Phone: {selectedPatient.phone}
              </p>
            </div>

            <h4 className="text-[10px] text-slate-400 font-extrabold uppercase mb-2 tracking-widest">PREVIOUS APPOINTMENTS</h4>
            {loadingHistory ? (
              <Skeleton height="75px" className="rounded-xl" />
            ) : history.length > 0 ? (
              <div className="space-y-2.5 max-h-60 overflow-y-auto pr-1 scrollbar-thin">
                {history.map(appt => (
                  <div key={appt._id} className="p-3.5 border border-slate-100 rounded-2xl flex justify-between items-center text-xs bg-slate-50/50 shadow-xs">
                    <div>
                      <p className="font-extrabold text-slate-800">{appt.date} at {appt.time}</p>
                      <p className="text-[9px] text-slate-400 font-bold uppercase tracking-wider mt-1">Token: #{appt.tokenNumber}</p>
                    </div>
                    <StatusBadge status={appt.status} className="shadow-xs" />
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-xs text-slate-450 font-semibold text-center py-8 border border-dashed border-slate-100 rounded-2xl bg-slate-50/20">No previous consultations recorded.</p>
            )}

            <Button onClick={() => setSelectedPatient(null)} className="w-full mt-6 py-2.5 rounded-xl text-xs font-bold shadow-md">
              Close History Log
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default DoctorPatients;

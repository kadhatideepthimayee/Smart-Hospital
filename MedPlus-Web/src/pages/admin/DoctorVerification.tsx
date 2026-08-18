import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getPendingDoctors, verifyDoctor } from '../../api/admin';
import { Card, Button, StatusBadge, Skeleton, Modal, Toast } from '../../components/UI';
import { ShieldAlert, Award, FileText, CheckCircle, XCircle } from 'lucide-react';
import { DoctorProfile } from '../../types';

const DoctorVerification: React.FC = () => {
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Detail Modal
  const [selectedDoctor, setSelectedDoctor] = useState<DoctorProfile | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');
  const [showRejectForm, setShowRejectForm] = useState(false);

  // 1. Fetch pending doctors
  const { data: pendingDoctors = [], isLoading } = useQuery({
    queryKey: ['pendingDoctors'],
    queryFn: () => getPendingDoctors('PENDING'),
  });

  // Verify doctor profile mutation
  const verifyMutation = useMutation({
    mutationFn: ({ doctorId, status, reason }: { doctorId: string; status: 'VERIFIED' | 'APPROVED' | 'REJECTED'; reason?: string }) =>
      verifyDoctor(doctorId, status, reason),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['pendingDoctors'] });
      queryClient.invalidateQueries({ queryKey: ['adminAllDoctors'] });
      setToast({ message: `Doctor profile verification marked as ${variables.status.toLowerCase()}.`, type: 'success' });
      setTimeout(() => setToast(null), 3000);
      setSelectedDoctor(null);
      setShowRejectForm(false);
      setRejectionReason('');
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Verification update failed.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleVerify = (doctorId: string, status: 'VERIFIED' | 'REJECTED', reason = '') => {
    verifyMutation.mutate({ doctorId, status, reason });
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="170px" className="rounded-3xl" />
          <Skeleton height="170px" className="rounded-3xl" />
        </div>
      ) : pendingDoctors.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {pendingDoctors.map(doc => (
            <Card key={doc.uid} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div>
                <div className="flex items-center gap-3.5 mb-4 select-none">
                  <div className="w-12 h-12 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-sm uppercase shadow-xs">
                    {doc.fullName.substring(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <h4 className="font-extrabold text-sm text-slate-850 leading-snug">Dr. {doc.fullName}</h4>
                    <span className="text-[10px] text-slate-400 font-extrabold uppercase tracking-widest mt-0.5 block">{doc.specialization} specialist</span>
                  </div>
                </div>

                <div className="flex flex-col gap-2 mb-4 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-500">
                  <div className="flex justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400">Qualification</span>
                    <span className="text-slate-800 font-bold">{doc.qualification || 'MBBS'}</span>
                  </div>
                  <div className="flex justify-between bg-slate-50/50 p-2 px-3 rounded-xl border border-slate-100/50">
                    <span className="text-slate-400">Experience</span>
                    <span className="text-slate-800 font-bold">{doc.experienceYears || 0} Years</span>
                  </div>
                </div>
              </div>

              <Button 
                onClick={() => { setSelectedDoctor(doc); setShowRejectForm(false); }} 
                className="w-full mt-3 py-2.5 text-xs rounded-xl font-bold shadow-xs hover:-translate-y-0.5 active:translate-y-0"
              >
                Review Application
              </Button>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-14 h-14 bg-slate-50 text-slate-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-slate-100/70 shadow-xs">
            <ShieldAlert size={24} />
          </div>
          <h3 className="text-base font-extrabold text-slate-800">No Pending Applications</h3>
          <p className="text-xs text-slate-450 font-semibold mt-2 max-w-xs mx-auto leading-relaxed">All doctor profile submissions have been verified.</p>
        </Card>
      )}

      {/* Review Details Modal */}
      <Modal
        isOpen={selectedDoctor !== null}
        onClose={() => setSelectedDoctor(null)}
        title="Professional Profile Review"
      >
        {selectedDoctor && (
          <div className="space-y-5 text-xs font-bold text-slate-655 select-none">
            <div className="border-b border-slate-100 pb-4 select-none">
              <h4 className="text-[9px] text-slate-400 font-extrabold uppercase tracking-widest mb-1.5">Applicant Name</h4>
              <p className="font-black text-base text-medical-blue-700 leading-snug">Dr. {selectedDoctor.fullName}</p>
              <p className="text-[10px] text-slate-450 font-extrabold uppercase tracking-wider mt-1">{selectedDoctor.specialization} Specialty</p>
            </div>

            <div className="space-y-3">
              <div className="flex justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Qualification</span>
                <span className="text-slate-800 font-extrabold">{selectedDoctor.qualification || 'MBBS'}</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Experience</span>
                <span className="text-slate-800 font-extrabold">{selectedDoctor.experienceYears || 0} Years</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Requested Fee</span>
                <span className="text-medical-teal-650 font-black text-sm">${selectedDoctor.consultationFee || 15}</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Reg Authority</span>
                <span className="text-slate-800 font-extrabold">{selectedDoctor.registrationAuthority || 'N/A'}</span>
              </div>
              <div className="flex justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Reg Number</span>
                <span className="text-slate-800 font-extrabold">{selectedDoctor.registrationNumber || 'N/A'}</span>
              </div>
              {selectedDoctor.registrationCertificateUrl && (
                <div className="flex justify-between border-b border-slate-100 pb-2.5">
                  <span className="text-slate-400">Certificate Doc</span>
                  <a 
                    href={selectedDoctor.registrationCertificateUrl} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-medical-blue-600 hover:text-medical-blue-800 font-extrabold underline decoration-2 decoration-medical-blue-300"
                  >
                    View Document
                  </a>
                </div>
              )}
              {selectedDoctor.verificationDocumentUrl && (
                <div className="flex justify-between border-b border-slate-100 pb-2.5">
                  <span className="text-slate-400">Identity Doc</span>
                  <a 
                    href={selectedDoctor.verificationDocumentUrl} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="text-medical-blue-600 hover:text-medical-blue-800 font-extrabold underline decoration-2 decoration-medical-blue-300"
                  >
                    View Document
                  </a>
                </div>
              )}
            </div>

            {selectedDoctor.bio && (
              <div className="bg-slate-50 p-4 border border-slate-100 rounded-2xl shadow-inner">
                <p className="font-extrabold mb-2 uppercase text-[9px] text-slate-400 tracking-widest">APPLICANT BIO</p>
                <p className="leading-relaxed text-slate-600 font-semibold">{selectedDoctor.bio}</p>
              </div>
            )}

            {!showRejectForm ? (
              <div className="flex gap-3 pt-4">
                <Button 
                  onClick={() => handleVerify(selectedDoctor.uid, 'VERIFIED')} 
                  className="flex-1 py-2.5 text-xs bg-emerald-650 hover:bg-emerald-705 active:bg-emerald-800 text-white border-0 shadow-md shadow-emerald-500/5 rounded-xl font-bold hover:-translate-y-0.5 active:translate-y-0"
                  loading={verifyMutation.isPending}
                >
                  Approve Verification
                </Button>
                <Button 
                  onClick={() => setShowRejectForm(true)} 
                  className="flex-1 py-2.5 text-xs bg-rose-650 hover:bg-rose-705 active:bg-rose-800 text-white border-0 shadow-md shadow-rose-500/5 rounded-xl font-bold hover:-translate-y-0.5 active:translate-y-0"
                >
                  Reject Request
                </Button>
              </div>
            ) : (
              <div className="pt-4 border-t border-slate-100 space-y-4">
                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">Rejection Reason</label>
                  <input
                    type="text"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                    placeholder="e.g. Qualification certificates missing/illegible"
                    value={rejectionReason}
                    onChange={(e) => setRejectionReason(e.target.value)}
                    required
                    disabled={verifyMutation.isPending}
                  />
                </div>
                <div className="flex gap-3">
                  <Button 
                    onClick={() => handleVerify(selectedDoctor.uid, 'REJECTED', rejectionReason)} 
                    className="flex-1 py-2.5 text-xs bg-rose-650 hover:bg-rose-705 active:bg-rose-800 text-white border-0 shadow-md shadow-rose-500/5 rounded-xl font-bold hover:-translate-y-0.5 active:translate-y-0"
                    loading={verifyMutation.isPending}
                    disabled={!rejectionReason.trim()}
                  >
                    Submit Rejection
                  </Button>
                  <Button 
                    type="button" 
                    onClick={() => setShowRejectForm(false)} 
                    variant="outline" 
                    className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-xs"
                  >
                    Cancel
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default DoctorVerification;

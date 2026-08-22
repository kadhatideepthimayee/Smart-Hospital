import React, { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getDoctorProfile, setupDoctorProfile } from '../../api/doctors';
import { Card, Button, StatusBadge, Toast } from '../../components/UI';
import { Award, ShieldCheck, FileText, CheckCircle } from 'lucide-react';
import { DoctorProfile as IDoctorProfile } from '../../types';

const SPECIALIZATIONS = [
  'General Medicine',
  'Cardiology',
  'Dermatology',
  'Pediatrics',
  'Neurology',
  'Orthopedics',
  'Gynecology'
];

const DoctorProfile: React.FC = () => {
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Form states
  const [specialization, setSpecialization] = useState('General Medicine');
  const [qualification, setQualification] = useState('');
  const [experienceYears, setExperienceYears] = useState(5);
  const [consultationFee, setConsultationFee] = useState(15);
  const [bio, setBio] = useState('');
  const [registrationAuthority, setRegistrationAuthority] = useState('');
  const [registrationNumber, setRegistrationNumber] = useState('');
  const [registrationCertificateUrl, setRegistrationCertificateUrl] = useState('');
  const [verificationDocumentUrl, setVerificationDocumentUrl] = useState('');

  // 1. Fetch Doctor Profile
  const { data: profile, isLoading } = useQuery({
    queryKey: ['doctorProfile'],
    queryFn: getDoctorProfile,
  });

  // Load profile values into state
  useEffect(() => {
    if (profile) {
      setSpecialization(profile.specialization || 'General Medicine');
      setQualification(profile.qualification || '');
      setExperienceYears(profile.experienceYears || 5);
      setConsultationFee(profile.consultationFee || 15);
      setBio(profile.bio || '');
      setRegistrationAuthority(profile.registrationAuthority || '');
      setRegistrationNumber(profile.registrationNumber || '');
      setRegistrationCertificateUrl(profile.registrationCertificateUrl || '');
      setVerificationDocumentUrl(profile.verificationDocumentUrl || '');
    }
  }, [profile]);

  // Mutation for updating profile
  const updateMutation = useMutation({
    mutationFn: setupDoctorProfile,
    onSuccess: (updatedProfile) => {
      queryClient.setQueryData(['doctorProfile'], updatedProfile);
      queryClient.invalidateQueries({ queryKey: ['doctorProfile'] });
      setToast({ message: 'Doctor profile details saved successfully.', type: 'success' });
      setTimeout(() => setToast(null), 4000);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to update professional profile.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    if (!qualification.trim()) {
      setToast({ message: 'Please enter your professional qualifications.', type: 'error' });
      return;
    }
    if (!registrationAuthority.trim() || !registrationNumber.trim()) {
      setToast({ message: 'Please enter your Registration Authority and Registration Number.', type: 'error' });
      return;
    }

    const payload: Partial<IDoctorProfile> = {
      specialization,
      qualification: qualification.trim(),
      experienceYears: Number(experienceYears),
      consultationFee: Number(consultationFee),
      bio: bio.trim(),
      registrationAuthority: registrationAuthority.trim(),
      registrationNumber: registrationNumber.trim(),
      registrationCertificateUrl: registrationCertificateUrl.trim(),
      verificationDocumentUrl: verificationDocumentUrl.trim(),
      workingDays: profile?.workingDays || ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'],
      consultationStartTime: profile?.consultationStartTime || '09:00 AM',
      consultationEndTime: profile?.consultationEndTime || '05:00 PM',
      lunchStartTime: profile?.lunchStartTime || '01:00 PM',
      lunchEndTime: profile?.lunchEndTime || '02:00 PM',
      breakStartTime: profile?.breakStartTime || '',
      breakEndTime: profile?.breakEndTime || '',
      slotDuration: profile?.slotDuration || 15,
      verificationStatus: profile?.verificationStatus === 'DRAFT' ? 'PENDING' : profile?.verificationStatus || 'PENDING'
    };

    updateMutation.mutate(payload);
  };

  const getInitials = (name?: string) => {
    if (!name) return 'DR';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto animate-in fade-in duration-300">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      {isLoading ? (
        <Card className="py-16 flex justify-center items-center text-xs font-bold text-slate-400 rounded-3xl" hoverEffect={false}>
          Loading professional details...
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          
          {/* Status view column */}
          <div className="md:col-span-1">
            <Card className="text-center p-6 rounded-3xl" hoverEffect={true}>
              <div className="w-16 h-16 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-xl mx-auto mb-4 uppercase shadow-xs select-none">
                {getInitials(profile?.fullName)}
              </div>
              <div className="flex items-center justify-center gap-1.5 select-none">
                <h3 className="font-extrabold text-sm text-slate-900 leading-snug">Dr. {profile?.fullName}</h3>
                {profile?.verificationStatus === 'VERIFIED' && <ShieldCheck size={16} className="text-emerald-500 flex-shrink-0" />}
              </div>
              <p className="text-[10px] text-slate-400 font-extrabold uppercase mt-1 tracking-widest select-none">
                {profile?.specialization || 'General Care'}
              </p>
              
              <div className="border-t border-slate-100 pt-5 mt-5">
                <span className="text-[9px] text-slate-400 font-extrabold block mb-2.5 tracking-widest uppercase select-none">VERIFICATION STATUS</span>
                <StatusBadge status={profile?.verificationStatus || 'PENDING'} className="shadow-xs" />
              </div>
            </Card>
          </div>

          {/* Form setup column */}
          <div className="md:col-span-2">
            <Card className="p-6 rounded-3xl" hoverEffect={false}>
              <h3 className="text-sm font-bold text-slate-800 tracking-tight mb-6 border-b border-slate-100 pb-3.5 flex items-center gap-2 select-none">
                <Award size={18} className="text-medical-blue-600 flex-shrink-0" /> 
                <span>Medical Credentials</span>
              </h3>

              <div className="bg-amber-50/70 border border-amber-200/50 text-amber-800 rounded-xl p-3.5 text-xs font-semibold mb-6 flex items-start gap-2.5 shadow-xs select-none">
                <ShieldCheck size={16} className="text-amber-600 flex-shrink-0 mt-0.5" />
                <span>Your professional credentials and verification details are locked and read-only. For scheduling and calendar updates, please visit the <strong>Availability</strong> section.</span>
              </div>

              <form onSubmit={(e) => e.preventDefault()} className="space-y-4">
                
                {/* Specialization selection */}
                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Specialty Specialization</label>
                  <select
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                    value={specialization}
                    onChange={(e) => setSpecialization(e.target.value)}
                    disabled={true}
                  >
                    {SPECIALIZATIONS.map(spec => (
                      <option key={spec} value={spec}>{spec}</option>
                    ))}
                  </select>
                </div>

                {/* Qualification input */}
                <div className="space-y-1.5">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Professional Qualification</label>
                  <input
                    type="text"
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                    placeholder="e.g. MBBS, MD (Cardiology)"
                    value={qualification}
                    onChange={(e) => setQualification(e.target.value)}
                    required
                    disabled={true}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  {/* Experience */}
                  <div className="space-y-1.5">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Experience (Years)</label>
                    <input
                      type="number"
                      min={0}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                      value={experienceYears}
                      onChange={(e) => setExperienceYears(Number(e.target.value))}
                      required
                      disabled={true}
                    />
                  </div>

                  {/* Consultation Fee */}
                  <div className="space-y-1.5">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Consultation Fee ($)</label>
                    <input
                      type="number"
                      min={0}
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                      value={consultationFee}
                      onChange={(e) => setConsultationFee(Number(e.target.value))}
                      required
                      disabled={true}
                    />
                  </div>
                </div>

                {/* Licensing Section */}
                <h4 className="text-xs font-bold text-slate-800 tracking-tight border-b border-slate-100 pb-2 mt-6 select-none flex items-center gap-2">
                  <FileText size={14} className="text-medical-blue-600" />
                  <span>Licensing & Verification Documents</span>
                </h4>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Registration Authority</label>
                    <input
                      type="text"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                      placeholder="e.g. State Medical Council"
                      value={registrationAuthority}
                      onChange={(e) => setRegistrationAuthority(e.target.value)}
                      required
                      disabled={true}
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Registration Number</label>
                    <input
                      type="text"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                      placeholder="e.g. Reg-7890"
                      value={registrationNumber}
                      onChange={(e) => setRegistrationNumber(e.target.value)}
                      required
                      disabled={true}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Certificate Document URL</label>
                    <input
                      type="text"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                      placeholder="e.g. http://site.com/certificate.pdf"
                      value={registrationCertificateUrl}
                      onChange={(e) => setRegistrationCertificateUrl(e.target.value)}
                      disabled={true}
                    />
                  </div>

                  <div className="space-y-1.5">
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Identity Document URL</label>
                    <input
                      type="text"
                      className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-slate-50 cursor-not-allowed"
                      placeholder="e.g. http://site.com/id.jpg"
                      value={verificationDocumentUrl}
                      onChange={(e) => setVerificationDocumentUrl(e.target.value)}
                      disabled={true}
                    />
                  </div>
                </div>

                {/* Biography */}
                <div className="space-y-1.5 mt-4">
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Clinical Bio / Description</label>
                  <textarea
                    className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs leading-relaxed font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all resize-none bg-slate-50 cursor-not-allowed"
                    rows={4}
                    placeholder="Briefly describe your clinical specialties, treatment focus, and patient care philosophy..."
                    value={bio}
                    onChange={(e) => setBio(e.target.value)}
                    disabled={true}
                  />
                </div>

                <div className="bg-slate-50 border border-slate-200/60 rounded-xl p-4 mt-6 text-center select-none">
                  <p className="text-xs font-bold text-slate-500">
                    Profile credentials and verification details are locked.
                  </p>
                </div>
              </form>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorProfile;

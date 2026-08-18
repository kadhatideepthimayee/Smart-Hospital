import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { getDoctorProfile, getDoctorQueue } from '../../api/doctors';
import { getDoctorAppointments } from '../../api/appointments';
import { Card, Button, StatusBadge, Skeleton } from '../../components/UI';
import { formatDateToBackend } from '../../lib/utils';
import { 
  Users, 
  Calendar, 
  CheckCircle, 
  ShieldAlert, 
  ChevronRight, 
  Stethoscope, 
  Clock, 
  ShieldCheck,
  Award,
  DollarSign
} from 'lucide-react';

const DoctorDashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // 1. Fetch Doctor Profile
  const { data: profile, isLoading: isProfileLoading } = useQuery({
    queryKey: ['doctorProfile'],
    queryFn: getDoctorProfile,
  });

  // 2. Fetch Doctor's appointments
  const { data: appointments = [], isLoading: isAppointmentsLoading } = useQuery({
    queryKey: ['doctorAppointments'],
    queryFn: getDoctorAppointments,
  });

  // 3. Fetch Doctor's queue
  const { data: queue = [], isLoading: isQueueLoading } = useQuery({
    queryKey: ['doctorQueue'],
    queryFn: () => getDoctorQueue(),
  });

  const loading = isProfileLoading || isAppointmentsLoading || isQueueLoading;

  // Filter today's appointments
  const getTodayAppts = () => {
    const todayStr = formatDateToBackend(new Date());
    return appointments.filter(appt => appt.date === todayStr);
  };

  const todayAppts = getTodayAppts();
  const completedToday = todayAppts.filter(appt => appt.status === 'COMPLETED').length;
  const pendingVerification = profile?.verificationStatus !== 'VERIFIED' && profile?.verificationStatus !== 'APPROVED';

  const getInitials = (name?: string) => {
    if (!name) return 'DR';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      
      {loading ? (
        <div className="space-y-6">
          <Skeleton height="110px" className="rounded-3xl" />
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Skeleton height="200px" className="rounded-3xl" />
            <Skeleton height="200px" className="rounded-3xl" mdColSpan={2} />
          </div>
        </div>
      ) : pendingVerification ? (
        <div className="max-w-2xl mx-auto py-4">
          <Card className="p-8 rounded-3xl border border-slate-100/75 shadow-lg bg-white relative overflow-hidden" hoverEffect={false}>
            {/* Background design accents */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-medical-blue-500/5 rounded-full blur-2xl -mr-16 -mt-16 pointer-events-none"></div>
            <div className="absolute bottom-0 left-0 w-32 h-32 bg-medical-teal-500/5 rounded-full blur-2xl -ml-16 -mb-16 pointer-events-none"></div>

            <div className="flex flex-col items-center text-center select-none mb-8">
              <div className={`p-4 rounded-2xl mb-5 shadow-xs border ${
                profile?.verificationStatus === 'REJECTED' 
                  ? 'bg-rose-50 border-rose-100 text-rose-600' 
                  : profile?.verificationStatus === 'PENDING'
                  ? 'bg-amber-50 border-amber-100 text-amber-600 animate-pulse'
                  : 'bg-medical-blue-50 border-medical-blue-100 text-medical-blue-600'
              }`}>
                <ShieldAlert size={36} />
              </div>
              <h3 className="text-lg font-black text-slate-900 tracking-tight">
                {profile?.verificationStatus === 'REJECTED' 
                  ? 'Verification Profile Rejected' 
                  : profile?.verificationStatus === 'PENDING'
                  ? 'Account Verification Under Review'
                  : 'Complete Your Professional Profile'
                }
              </h3>
              <p className="text-xs text-slate-450 mt-2 font-semibold max-w-lg leading-relaxed">
                {profile?.verificationStatus === 'REJECTED'
                  ? 'Your professional credentials did not pass the auditing process. Please review the comments below, edit your profile details, and resubmit.'
                  : profile?.verificationStatus === 'PENDING'
                  ? 'Your medical license and shift credentials have been submitted and are currently undergoing auditing. We will verify your account shortly.'
                  : 'Welcome to MedPlus! To start accepting appointments, setting consultation slots, and managing patients, please complete your professional details.'
                }
              </p>
            </div>

            {profile?.verificationStatus === 'REJECTED' && (
              <div className="mb-8 p-4 bg-rose-50/70 border border-rose-100 rounded-2xl text-left">
                <span className="text-[10px] text-rose-500 font-extrabold uppercase tracking-widest block mb-1">REJECTION COMMENTS</span>
                <p className="text-xs font-bold text-slate-700 leading-relaxed italic">
                  "{profile.rejectionReason || 'Uploaded documentation was incomplete or unclear.'}"
                </p>
              </div>
            )}

            {/* Steps Visual Tracker */}
            <div className="space-y-6 max-w-md mx-auto mb-8 text-left">
              <div className="flex items-start gap-4">
                <div className="w-6 h-6 rounded-full bg-emerald-500 text-white flex items-center justify-center text-[10px] font-black shrink-0 mt-0.5">✓</div>
                <div>
                  <h5 className="text-xs font-extrabold text-slate-800">Step 1: Account Setup</h5>
                  <p className="text-[10px] text-slate-450 font-semibold mt-0.5">Doctor sign-up completed successfully.</p>
                </div>
              </div>

              <div className="flex items-start gap-4">
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-black shrink-0 mt-0.5 ${
                  profile && profile.verificationStatus !== 'DRAFT'
                    ? 'bg-emerald-500 text-white'
                    : 'bg-medical-blue-100 text-medical-blue-600'
                }`}>
                  {profile && profile.verificationStatus !== 'DRAFT' ? '✓' : '2'}
                </div>
                <div>
                  <h5 className="text-xs font-extrabold text-slate-800">Step 2: Submit Credentials</h5>
                  <p className="text-[10px] text-slate-450 font-semibold mt-0.5">
                    {profile && profile.verificationStatus !== 'DRAFT'
                      ? `Credentials submitted successfully (${profile.verificationStatus === 'PENDING' ? 'Under Review' : 'Rejected'}).`
                      : 'Provide your credentials, certificates, working days, and fees.'
                    }
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-4">
                <div className="w-6 h-6 rounded-full bg-slate-100 text-slate-400 flex items-center justify-center text-[10px] font-black shrink-0 mt-0.5">3</div>
                <div>
                  <h5 className="text-xs font-extrabold text-slate-400">Step 3: Verification Check</h5>
                  <p className="text-[10px] text-slate-400 font-semibold mt-0.5">Administrators activate your workspace once verified.</p>
                </div>
              </div>
            </div>

            <div className="text-center border-t border-slate-100 pt-6">
              <Button
                onClick={() => navigate('/doctor/profile')}
                className="py-3 px-6 rounded-2xl text-xs font-black shadow-md bg-gradient-to-r from-medical-blue-600 to-medical-teal-650 text-white hover:-translate-y-0.5 active:translate-y-0"
              >
                {profile?.verificationStatus === 'REJECTED'
                  ? 'Update and Resubmit Credentials'
                  : profile?.verificationStatus === 'PENDING'
                  ? 'Review Submitted Credentials'
                  : 'Configure Professional Profile'
                }
              </Button>
            </div>
          </Card>
        </div>
      ) : (
        <>
          {/* Overview stats */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 select-none">
            <div className="bg-white border border-slate-100/75 p-6 rounded-3xl flex items-center justify-between shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05)] hover:shadow-md transition-all duration-300">
              <div>
                <span className="text-[10px] text-slate-400 font-extrabold block mb-1.5 uppercase tracking-widest">TODAY'S WORKLOAD</span>
                <p className="text-2xl font-black text-slate-900">{todayAppts.length} Appts</p>
              </div>
              <div className="p-3 bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 rounded-2xl shadow-xs">
                <Calendar size={22} />
              </div>
            </div>

            <div className="bg-white border border-slate-100/75 p-6 rounded-3xl flex items-center justify-between shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05)] hover:shadow-md transition-all duration-300">
              <div>
                <span className="text-[10px] text-slate-400 font-extrabold block mb-1.5 uppercase tracking-widest">ACTIVE QUEUE</span>
                <p className="text-2xl font-black text-amber-600">{queue.filter(q => q.status === 'WAITING').length} Patients</p>
              </div>
              <div className="p-3 bg-amber-50/70 border border-amber-100/50 text-amber-600 rounded-2xl shadow-xs">
                <Users size={22} />
              </div>
            </div>

            <div className="bg-white border border-slate-100/75 p-6 rounded-3xl flex items-center justify-between shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05)] hover:shadow-md transition-all duration-300">
              <div>
                <span className="text-[10px] text-slate-400 font-extrabold block mb-1.5 uppercase tracking-widest">COMPLETED TODAY</span>
                <p className="text-2xl font-black text-emerald-600">{completedToday} / {todayAppts.length}</p>
              </div>
              <div className="p-3 bg-emerald-50/70 border border-emerald-100/50 text-emerald-600 rounded-2xl shadow-xs">
                <CheckCircle size={22} />
              </div>
            </div>
          </div>

          {/* Split grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            
            {/* Doctor Specialty card */}
            <Card className="md:col-span-1 flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div className="text-center py-4 border-b border-slate-150/40 select-none">
                <div className="w-16 h-16 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-xl mx-auto mb-4 uppercase shadow-xs">
                  {getInitials(profile?.fullName)}
                </div>
                <div className="flex items-center justify-center gap-1">
                  <h3 className="font-extrabold text-sm text-slate-900">Dr. {profile?.fullName || user?.fullName}</h3>
                  {profile?.verificationStatus === 'VERIFIED' && <ShieldCheck size={15} className="text-emerald-500 flex-shrink-0" />}
                </div>
                <span className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest mt-1 block">{profile?.specialization || 'Clinical'} Specialist</span>
              </div>

              <div className="flex flex-col gap-3.5 mt-5 text-xs font-semibold text-slate-650">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400 flex items-center gap-1.5"><Award size={13} className="text-slate-400" /> Experience</span>
                  <span className="text-slate-800 font-bold">{profile?.experienceYears || 0} Years</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400 flex items-center gap-1.5"><Stethoscope size={13} className="text-slate-400" /> Qualification</span>
                  <span className="text-slate-800 font-bold">{profile?.qualification || 'MBBS'}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400 flex items-center gap-1.5"><DollarSign size={13} className="text-slate-400" /> Consultation Fee</span>
                  <span className="text-medical-teal-650 font-extrabold">${profile?.consultationFee || 15}</span>
                </div>
              </div>
              
              <Button 
                onClick={() => navigate('/doctor/profile')} 
                variant="outline" 
                className="w-full mt-6 py-2.5 rounded-xl text-xs font-bold shadow-xs hover:-translate-y-0.5 active:translate-y-0"
              >
                Manage Profile
              </Button>
            </Card>

            {/* Today's Appointment Log */}
            <Card className="md:col-span-2 p-6 rounded-3xl" hoverEffect={false}>
              <div className="flex justify-between items-center mb-5 select-none">
                <div>
                  <h3 className="text-base font-extrabold text-slate-850 tracking-tight">Today's Appointment Log</h3>
                  <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">Visits scheduled for today</p>
                </div>
                <Button 
                  onClick={() => navigate('/doctor/appointments')} 
                  variant="outline" 
                  className="py-2 px-3 text-xs flex items-center gap-1 rounded-xl font-bold shadow-xs hover:-translate-y-0.5 bg-white"
                >
                  <span>View All</span>
                  <ChevronRight size={14} />
                </Button>
              </div>

              {todayAppts.length > 0 ? (
                <div className="space-y-3.5">
                  {todayAppts.slice(0, 5).map(appt => (
                    <div key={appt._id} className="flex justify-between items-center p-4 border border-slate-100 rounded-2xl hover:bg-slate-50/50 transition-colors shadow-xs">
                      <div className="space-y-0.5">
                        <h4 className="font-extrabold text-xs text-slate-850">Patient: {appt.patientName}</h4>
                        <p className="text-[9px] text-slate-450 font-extrabold uppercase tracking-wider flex items-center gap-1.5">
                          <Clock size={11} className="text-slate-350" />
                          <span>{appt.time}</span>
                          <span>&bull;</span>
                          <span>Token #{appt.tokenNumber}</span>
                        </p>
                      </div>
                      <StatusBadge status={appt.status} className="shadow-xs" />
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-16 text-xs font-semibold text-slate-400 border border-dashed border-slate-200 rounded-2xl bg-slate-50/50 select-none">
                  No appointments booked for today.
                </div>
              )}
            </Card>
          </div>
        </>
      )}
    </div>
  );
};

export default DoctorDashboard;

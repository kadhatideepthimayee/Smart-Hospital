import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { getUpcomingAppointment, getLiveQueueTracking } from '../../api/dashboard';
import { Card, Button, StatusBadge, Skeleton } from '../../components/UI';
import { 
  CalendarRange, 
  Activity, 
  FileText, 
  Bell, 
  Clock, 
  AlertTriangle,
  ArrowRight,
  ListOrdered,
  User,
  MapPin,
  CalendarCheck
} from 'lucide-react';

const PatientDashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  // 1. Fetch upcoming appointment
  const { data: upcoming, isLoading: isUpcomingLoading } = useQuery({
    queryKey: ['upcomingAppointment'],
    queryFn: getUpcomingAppointment,
  });

  // 2. Fetch live queue status with periodic polling
  const { data: liveQueue, isLoading: isQueueLoading } = useQuery({
    queryKey: ['liveQueueStatus'],
    queryFn: () => getLiveQueueTracking(),
    refetchInterval: 12000, // Poll every 12 seconds
  });

  const isLoading = isUpcomingLoading || isQueueLoading;

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Welcome Banner */}
      <div className="bg-gradient-to-r from-medical-blue-600 via-medical-blue-700 to-medical-teal-600 text-white rounded-3xl p-8 md:p-10 flex flex-col md:flex-row md:items-center justify-between gap-6 shadow-lg shadow-medical-blue-500/10 relative overflow-hidden select-none">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom_right,_var(--tw-gradient-stops))] from-white/10 via-transparent to-transparent"></div>
        <div className="z-10">
          <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight">Welcome Back, {user?.fullName}!</h1>
          <p className="text-white/80 text-sm max-w-lg mt-2 font-medium leading-relaxed">
            Track your appointments, view live queue statuses, and consult with leading medical practitioners all in one secure portal.
          </p>
        </div>
        <Button 
          onClick={() => navigate('/patient/book')} 
          className="bg-white hover:bg-slate-50 active:bg-slate-100 text-slate-800 font-bold self-start md:self-center py-3 px-6 shadow-md z-10 hover:-translate-y-[2px]"
        >
          Book Appointment
        </Button>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="230px" className="rounded-3xl" />
          <Skeleton height="230px" className="rounded-3xl" />
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          
          {/* Upcoming Appointment Card */}
          <Card className="flex flex-col justify-between p-7 rounded-3xl" hoverEffect={true}>
            <div>
              <div className="flex items-center gap-3.5 mb-6">
                <div className="p-3 bg-medical-blue-50/70 border border-medical-blue-100/50 rounded-xl text-medical-blue-600">
                  <CalendarRange size={22} />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-900 tracking-tight">Upcoming Consultation</h3>
                  <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5">Your next scheduled visit</p>
                </div>
              </div>

              {upcoming ? (
                <div className="animate-in fade-in duration-200 bg-slate-50/60 border border-slate-100/60 p-4 rounded-2xl">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-xl bg-medical-blue-100/50 border border-medical-blue-200/50 flex items-center justify-center text-medical-blue-700 font-extrabold text-sm uppercase select-none">
                      {upcoming.doctorName ? upcoming.doctorName.split(' ').map((n: string) => n[0]).join('').substring(0, 2) : 'DR'}
                    </div>
                    <div>
                      <p className="font-extrabold text-slate-800">Dr. {upcoming.doctorName}</p>
                      <p className="text-xs text-slate-500 font-bold flex items-center gap-1.5 mt-0.5">
                        <MapPin size={12} className="text-slate-400" />
                        {upcoming.department}
                      </p>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-2 gap-4 border-t border-slate-150/50 mt-4 pt-4">
                    <div>
                      <p className="text-[10px] text-slate-400 font-bold tracking-wider uppercase">Scheduled Date</p>
                      <p className="text-sm font-extrabold text-slate-800 mt-0.5">{upcoming.date}</p>
                    </div>
                    <div>
                      <p className="text-[10px] text-slate-400 font-bold tracking-wider uppercase">Time Slot</p>
                      <p className="text-sm font-extrabold text-slate-800 mt-0.5">{upcoming.time}</p>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center py-8 flex flex-col items-center justify-center h-[130px] bg-slate-50/50 border border-dashed border-slate-200 rounded-2xl">
                  <p className="text-sm text-slate-500 font-semibold">No upcoming consultations booked.</p>
                  <Button 
                    onClick={() => navigate('/patient/book')} 
                    variant="outline" 
                    className="text-xs py-2 px-4 mt-3 rounded-xl hover:-translate-y-0.5 active:translate-y-0 shadow-xs"
                  >
                    Book Now
                  </Button>
                </div>
              )}
            </div>

            {upcoming && (
              <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end">
                <Button 
                  onClick={() => navigate('/patient/appointments')} 
                  variant="outline" 
                  className="text-xs py-2 px-4 rounded-xl flex items-center gap-1.5 hover:-translate-y-[1px] active:translate-y-0 shadow-xs font-bold"
                >
                  <span>View Details</span>
                  <ArrowRight size={14} />
                </Button>
              </div>
            )}
          </Card>

          {/* Live Queue tracking Card */}
          <Card className="flex flex-col justify-between p-7 rounded-3xl" hoverEffect={true}>
            <div>
              <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3.5">
                  <div className="p-3 bg-amber-50/70 border border-amber-100/50 rounded-xl text-amber-600">
                    <Clock size={22} />
                  </div>
                  <div>
                    <h3 className="text-base font-bold text-slate-900 tracking-tight">Live Queue Status</h3>
                    <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5">Real-time status tracking</p>
                  </div>
                </div>
                {liveQueue && <StatusBadge status={liveQueue.status} />}
              </div>

              {liveQueue && liveQueue.isActive ? (
                <div className="animate-in fade-in duration-200 bg-slate-50/60 border border-slate-100/60 p-4 rounded-2xl">
                  <div className="flex items-center justify-between border-b border-slate-150/50 pb-4 mb-4 select-none">
                    <div>
                      <p className="text-[10px] text-slate-400 font-bold tracking-wider uppercase">Your Token</p>
                      <p className="text-2xl font-black text-medical-blue-600 mt-0.5">#{liveQueue.queueNumber}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-[10px] text-slate-400 font-bold tracking-wider uppercase">Serving Now</p>
                      <p className="text-2xl font-black text-slate-800 mt-0.5">#{liveQueue.currentServingToken}</p>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4 select-none">
                    <div>
                      <p className="text-[10px] text-slate-400 font-bold tracking-wider uppercase">Patients Ahead</p>
                      <p className="text-base font-extrabold text-slate-800 mt-0.5">{liveQueue.patientsAhead}</p>
                    </div>
                    <div>
                      <p className="text-[10px] text-slate-400 font-bold tracking-wider uppercase">Est. Wait Time</p>
                      <p className="text-base font-extrabold text-medical-teal-600 mt-0.5">{liveQueue.estimatedWaitMinutes} mins</p>
                    </div>
                  </div>

                  {liveQueue.status === 'DOCTOR_RUNNING_LATE' && (
                    <div className="mt-4 flex items-center gap-3 p-3 bg-rose-50/70 border border-rose-100 rounded-xl text-xs text-rose-700 font-semibold leading-relaxed animate-pulse">
                      <AlertTriangle size={15} className="flex-shrink-0 text-rose-500" />
                      <span>Doctor is running behind schedule. Expect delay alerts.</span>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-center py-8 flex flex-col items-center justify-center h-[130px] bg-slate-50/50 border border-dashed border-slate-200 rounded-2xl select-none">
                  <p className="text-sm text-slate-500 font-semibold">No active queue tracker running.</p>
                  <p className="text-[10px] text-slate-400 font-bold mt-1.5 uppercase tracking-wider">Trackers activate on the day of appointment.</p>
                </div>
              )}
            </div>

            {liveQueue && liveQueue.isActive && (
              <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end">
                <Button 
                  onClick={() => navigate('/patient/queue')} 
                  className="w-full text-xs py-2.5 rounded-xl font-bold shadow-md hover:-translate-y-0.5 active:translate-y-0"
                >
                  Track Live Queue
                </Button>
              </div>
            )}
          </Card>
        </div>
      )}

      {/* Quick Actions Portal */}
      <div>
        <h3 className="text-lg font-bold text-slate-900 tracking-tight mb-5">Quick Actions</h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 select-none">
          <div 
            onClick={() => navigate('/patient/book')} 
            className="bg-white border border-slate-100 rounded-2xl p-6 text-center cursor-pointer hover:border-medical-blue-500 hover:shadow-[0_8px_30px_rgba(0,0,0,0.03)] hover:-translate-y-1 transition-all duration-300 flex flex-col items-center gap-3 group shadow-xs"
          >
            <div className="w-12 h-12 rounded-2xl bg-medical-blue-50 border border-medical-blue-100/30 text-medical-blue-600 flex items-center justify-center group-hover:scale-105 group-hover:bg-medical-blue-600 group-hover:text-white transition-all duration-300 shadow-sm shadow-medical-blue-500/5">
              <CalendarRange size={24} />
            </div>
            <span className="text-xs font-extrabold text-slate-700 group-hover:text-medical-blue-600 transition-colors">Book Slot</span>
          </div>

          <div 
            onClick={() => navigate('/patient/appointments')} 
            className="bg-white border border-slate-100 rounded-2xl p-6 text-center cursor-pointer hover:border-medical-blue-500 hover:shadow-[0_8px_30px_rgba(0,0,0,0.03)] hover:-translate-y-1 transition-all duration-300 flex flex-col items-center gap-3 group shadow-xs"
          >
            <div className="w-12 h-12 rounded-2xl bg-medical-blue-50 border border-medical-blue-100/30 text-medical-blue-600 flex items-center justify-center group-hover:scale-105 group-hover:bg-medical-blue-600 group-hover:text-white transition-all duration-300 shadow-sm shadow-medical-blue-500/5">
              <ListOrdered size={24} />
            </div>
            <span className="text-xs font-extrabold text-slate-700 group-hover:text-medical-blue-600 transition-colors">My Appointments</span>
          </div>

          <div 
            onClick={() => navigate('/patient/records')} 
            className="bg-white border border-slate-100 rounded-2xl p-6 text-center cursor-pointer hover:border-medical-blue-500 hover:shadow-[0_8px_30px_rgba(0,0,0,0.03)] hover:-translate-y-1 transition-all duration-300 flex flex-col items-center gap-3 group shadow-xs"
          >
            <div className="w-12 h-12 rounded-2xl bg-medical-blue-50 border border-medical-blue-100/30 text-medical-blue-600 flex items-center justify-center group-hover:scale-105 group-hover:bg-medical-blue-600 group-hover:text-white transition-all duration-300 shadow-sm shadow-medical-blue-500/5">
              <FileText size={24} />
            </div>
            <span className="text-xs font-extrabold text-slate-700 group-hover:text-medical-blue-600 transition-colors">Medical Records</span>
          </div>

          <div 
            onClick={() => navigate('/patient/notifications')} 
            className="bg-white border border-slate-100 rounded-2xl p-6 text-center cursor-pointer hover:border-medical-blue-500 hover:shadow-[0_8px_30px_rgba(0,0,0,0.03)] hover:-translate-y-1 transition-all duration-300 flex flex-col items-center gap-3 group shadow-xs"
          >
            <div className="w-12 h-12 rounded-2xl bg-medical-blue-50 border border-medical-blue-100/30 text-medical-blue-600 flex items-center justify-center group-hover:scale-105 group-hover:bg-medical-blue-600 group-hover:text-white transition-all duration-300 shadow-sm shadow-medical-blue-500/5">
              <Bell size={24} />
            </div>
            <span className="text-xs font-extrabold text-slate-700 group-hover:text-medical-blue-600 transition-colors">Notifications</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientDashboard;

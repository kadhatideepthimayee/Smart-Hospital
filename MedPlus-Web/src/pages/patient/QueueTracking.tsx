import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { getLiveQueueTracking, getUpcomingAppointment } from '../../api/dashboard';
import { Card, Button, StatusBadge, Skeleton } from '../../components/UI';
import { Clock, Users, AlertTriangle, RefreshCw, Radio } from 'lucide-react';

const QueueTracking: React.FC = () => {
  // 1. Fetch upcoming appointment to obtain doctor name and department context
  const { data: upcoming, isLoading: isUpcomingLoading } = useQuery({
    queryKey: ['upcomingAppointmentContext'],
    queryFn: getUpcomingAppointment,
  });

  // 2. Fetch live queue tracking stats (polled every 12s)
  const { data: liveQueue, isLoading: isQueueLoading, refetch, isRefetching } = useQuery({
    queryKey: ['liveQueueTracking'],
    queryFn: () => getLiveQueueTracking(),
    refetchInterval: 12000,
  });

  const loading = isUpcomingLoading || isQueueLoading;

  const handleManualRefresh = () => {
    refetch();
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-5 select-none">
        <div className="flex items-center gap-2">
          {liveQueue && liveQueue.isActive && (
            <span className="flex h-2.5 w-2.5 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
            </span>
          )}
          <p className="text-xs font-bold text-slate-400 max-w-md">
            Real-time consulting progress tracker synchronized with the clinic's front-desk console.
          </p>
        </div>
        <Button 
          onClick={handleManualRefresh} 
          variant="outline" 
          className="py-2 px-4 text-xs flex items-center gap-2 rounded-xl font-bold shadow-xs hover:-translate-y-0.5 active:translate-y-0"
          disabled={loading || isRefetching}
        >
          <RefreshCw size={12} className={isRefetching ? 'animate-spin' : ''} />
          <span>Force Sync</span>
        </Button>
      </div>

      {loading ? (
        <Card className="max-w-xl mx-auto py-12 space-y-6 rounded-3xl" hoverEffect={false}>
          <Skeleton height="75px" className="rounded-2xl" />
          <Skeleton height="35px" className="rounded-2xl" />
          <Skeleton height="110px" className="rounded-2xl" />
        </Card>
      ) : liveQueue && liveQueue.isActive ? (
        <div className="max-w-xl mx-auto animate-in fade-in duration-200">
          {/* Main Tracker Card */}
          <Card className="border-2 border-medical-blue-600/60 overflow-hidden !p-0 rounded-3xl" hoverEffect={false}>
            {/* Header info */}
            <div className="bg-slate-50/70 px-6 py-5 border-b border-slate-100 flex justify-between items-center select-none">
              <div>
                <h4 className="text-[9px] text-slate-400 font-extrabold uppercase tracking-widest flex items-center gap-1.5">
                  <Radio size={12} className="text-medical-blue-600 animate-pulse" />
                  CONSULTING WITH
                </h4>
                <p className="font-black text-base text-slate-900 mt-1">
                  {upcoming?.doctorName ? `Dr. {upcoming.doctorName}` : 'Clinic Specialist'}
                </p>
                <p className="text-[10px] text-slate-400 font-bold uppercase mt-1 tracking-wider">
                  {liveQueue.department || upcoming?.department || 'Medical Care'}
                </p>
              </div>
              <StatusBadge status={liveQueue.status} className="shadow-xs" />
            </div>

            <div className="p-6 md:p-8">
              {/* Token Counter Row */}
              <div className="flex flex-col sm:flex-row justify-around items-center gap-8 border-b border-slate-100 pb-8 mb-6">
                <div className="text-center">
                  <span className="text-[10px] text-slate-400 font-extrabold uppercase tracking-widest block mb-4 select-none">CURRENTLY SERVING</span>
                  <div className="relative inline-flex items-center justify-center">
                    <div className="absolute -inset-1 bg-gradient-to-tr from-medical-blue-600 to-medical-teal-500 rounded-full blur opacity-15 animate-pulse"></div>
                    <div className="w-24 h-24 rounded-full bg-gradient-to-tr from-medical-blue-600 to-medical-blue-700 flex items-center justify-center font-black text-2xl text-white shadow-lg shadow-medical-blue-500/20 border-4 border-white select-all relative">
                      #{liveQueue.currentServingToken}
                    </div>
                  </div>
                </div>

                <div className="w-px h-16 bg-slate-100 hidden sm:block"></div>

                <div className="text-center">
                  <span className="text-[10px] text-slate-400 font-extrabold uppercase tracking-widest block mb-4 select-none">YOUR TOKEN NUMBER</span>
                  <div className="w-24 h-24 rounded-full bg-slate-50 flex items-center justify-center font-black text-2xl text-slate-800 border-4 border-slate-200/50 shadow-sm select-all">
                    #{liveQueue.queueNumber}
                  </div>
                </div>
              </div>

              {/* Status details Grid */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-50/60 p-4.5 rounded-2xl border border-slate-100/70 text-center shadow-xs">
                  <span className="text-[10px] text-slate-450 font-bold uppercase block mb-1 select-none">Patients Ahead</span>
                  <div className="flex items-center justify-center gap-2 text-xl font-black text-slate-800 mt-1">
                    <Users size={18} className="text-slate-400" />
                    <span>{liveQueue.patientsAhead}</span>
                  </div>
                </div>

                <div className="bg-slate-50/60 p-4.5 rounded-2xl border border-slate-100/70 text-center shadow-xs">
                  <span className="text-[10px] text-slate-450 font-bold uppercase block mb-1 select-none">Estimated Wait</span>
                  <div className="flex items-center justify-center gap-2 text-xl font-black text-medical-teal-650 mt-1">
                    <Clock size={18} className="text-medical-teal-500" />
                    <span>{liveQueue.estimatedWaitMinutes}m</span>
                  </div>
                </div>
              </div>

              {/* Delay banner */}
              {liveQueue.status === 'DOCTOR_RUNNING_LATE' && (
                <div className="mt-6 flex items-start gap-3.5 p-4 bg-rose-50/70 border border-rose-100/60 rounded-2xl text-xs text-rose-700 leading-relaxed font-semibold">
                  <AlertTriangle size={18} className="flex-shrink-0 mt-0.5 text-rose-500" />
                  <div>
                    <p className="font-extrabold text-rose-900 mb-0.5">Doctor Running Behind Schedule</p>
                    <p className="text-rose-700/80 font-medium leading-relaxed">Waiting times are updated dynamically to reflect the current pace of consultations.</p>
                  </div>
                </div>
              )}
            </div>
          </Card>
        </div>
      ) : (
        <Card className="max-w-md mx-auto text-center py-12 select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-14 h-14 bg-slate-50 text-slate-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-slate-100/70 shadow-xs">
            <Clock size={24} />
          </div>
          <h3 className="text-base font-extrabold text-slate-800">No Active Queue Session</h3>
          <p className="text-xs text-slate-400 font-semibold max-w-xs mx-auto mt-2 leading-relaxed">
            Live queue counters activate on the day of your appointment when the doctor opens the clinic session.
          </p>
        </Card>
      )}
    </div>
  );
};

export default QueueTracking;

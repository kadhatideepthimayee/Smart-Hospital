import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getDoctorQueue, updateQueueStatus } from '../../api/doctors';
import { createMedicalRecord } from '../../api/medicalRecords';
import { Card, Button, StatusBadge, Skeleton, Modal, Toast } from '../../components/UI';
import { formatDateToBackend } from '../../lib/utils';
import { AlertTriangle, Clock, RefreshCw, CheckCircle, FileText, Pill, Calendar, UserCheck } from 'lucide-react';
import { QueueItem } from '../../types';

const DoctorQueue: React.FC = () => {
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Complete consultation modal details
  const [activeQueueItem, setActiveQueueItem] = useState<QueueItem | null>(null);
  const [diagnosis, setDiagnosis] = useState('');
  const [prescription, setPrescription] = useState('');
  const [notes, setNotes] = useState('');
  const [followUpDate, setFollowUpDate] = useState('');

  // 1. Fetch Doctor's queue
  const { data: queue = [], isLoading, refetch, isRefetching } = useQuery({
    queryKey: ['doctorQueue'],
    queryFn: () => getDoctorQueue(),
    refetchInterval: 10000, // Poll every 10 seconds
  });

  // Mutation for updating status (start consultation or delay)
  const statusMutation = useMutation({
    mutationFn: ({ queueId, newStatus }: { queueId: string; newStatus: string }) => 
      updateQueueStatus(queueId, newStatus),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['doctorQueue'] });
      queryClient.invalidateQueries({ queryKey: ['doctorAppointments'] });
      const statusText = variables.newStatus.replace('_', ' ').toLowerCase();
      setToast({ message: `Session status updated: ${statusText}.`, type: 'success' });
      setTimeout(() => setToast(null), 3000);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to update queue status.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  // Complete consultation mutation
  const completeMutation = useMutation({
    mutationFn: async () => {
      if (!activeQueueItem) return;

      // 1. Create medical record
      await createMedicalRecord({
        appointmentId: activeQueueItem.appointmentId,
        patientId: activeQueueItem.patientId,
        diagnosis: diagnosis.trim(),
        prescription: prescription.trim(),
        notes: notes.trim(),
        followUpDate: followUpDate ? formatDateToBackend(new Date(followUpDate)) : undefined
      });

      // 2. Mark queue status as COMPLETED
      await updateQueueStatus(activeQueueItem._id, 'COMPLETED');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctorQueue'] });
      queryClient.invalidateQueries({ queryKey: ['doctorAppointments'] });
      setToast({ message: `Consultation completed and prescription saved successfully.`, type: 'success' });
      setTimeout(() => setToast(null), 4000);
      setActiveQueueItem(null);
      setDiagnosis('');
      setPrescription('');
      setNotes('');
      setFollowUpDate('');
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to file medical record.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleStart = (item: QueueItem) => {
    statusMutation.mutate({ queueId: item._id, newStatus: 'IN_PROGRESS' });
  };

  const handleDelayToggle = (item: QueueItem) => {
    const nextStatus = item.status === 'DOCTOR_RUNNING_LATE' ? 'WAITING' : 'DOCTOR_RUNNING_LATE';
    statusMutation.mutate({ queueId: item._id, newStatus: nextStatus });
  };

  const handleOpenCompleteModal = (item: QueueItem) => {
    setActiveQueueItem(item);
    setDiagnosis('');
    setPrescription('');
    setNotes('');
    setFollowUpDate('');
  };

  const handleCompleteSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!diagnosis.trim()) {
      setToast({ message: 'Diagnosis field cannot be empty.', type: 'error' });
      return;
    }
    completeMutation.mutate();
  };

  // Find active or next serving item
  const getActiveItem = () => {
    return queue.find(item => item.status === 'IN_PROGRESS' || item.status === 'WAITING' || item.status === 'DOCTOR_RUNNING_LATE');
  };

  const currentActive = getActiveItem();
  const waitingPatients = queue.filter(item => item.status === 'WAITING' || item.status === 'DOCTOR_RUNNING_LATE');

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      
      {/* Toast Alert */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-5 select-none">
        <p className="text-xs font-bold text-slate-400">
          Control client consult pipelines, declare break limits, and post dynamic delay warnings.
        </p>
        <Button 
          onClick={() => refetch()} 
          variant="outline" 
          className="py-2 px-4 text-xs flex items-center gap-2 rounded-xl font-bold shadow-xs hover:-translate-y-0.5 active:translate-y-0 bg-white"
          disabled={isLoading || isRefetching}
        >
          <RefreshCw size={12} className={isRefetching ? 'animate-spin' : ''} />
          <span>Sync Queue</span>
        </Button>
      </div>

      {isLoading ? (
        <Card className="max-w-xl mx-auto py-12 rounded-3xl" hoverEffect={false}>
          <Skeleton height="150px" className="rounded-2xl" />
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Active Consultation Console */}
          <div className="lg:col-span-2 animate-in fade-in duration-200">
            <Card className="border-2 border-medical-blue-600/70 min-h-[380px] flex flex-col justify-between p-7 rounded-3xl" hoverEffect={false}>
              {currentActive ? (
                <div className="flex flex-col justify-between h-full">
                  <div>
                    <div className="flex justify-between items-start border-b border-slate-100 pb-4 mb-5">
                      <div>
                        <h3 className="font-extrabold text-slate-800 text-sm leading-snug">Active Patient Session</h3>
                        <p className="text-[10px] text-slate-400 font-bold uppercase mt-1 select-all font-mono tracking-tight">Appt ID: {currentActive.appointmentId}</p>
                      </div>
                      <StatusBadge status={currentActive.status} className="shadow-xs" />
                    </div>

                    <div className="flex items-center gap-4 my-6">
                      <div className="w-14 h-14 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-lg uppercase select-none shadow-xs">
                        {currentActive.patientName.substring(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <h4 className="font-extrabold text-sm text-slate-900 leading-snug">{currentActive.patientName}</h4>
                        <p className="text-xs text-slate-500 font-bold mt-1">
                          Assigned Queue Token: <span className="text-medical-blue-650 font-black text-sm">#{currentActive.tokenNumber}</span>
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Operational triggers */}
                  <div className="border-t border-slate-100 pt-5 flex flex-col sm:flex-row gap-3">
                    {currentActive.status === 'WAITING' || currentActive.status === 'DOCTOR_RUNNING_LATE' ? (
                      <Button 
                        onClick={() => handleStart(currentActive)} 
                        className="py-2.5 px-6 flex-1 text-xs rounded-xl font-bold shadow-md shadow-medical-blue-500/5 hover:-translate-y-0.5 active:translate-y-0"
                        loading={statusMutation.isPending}
                      >
                        Start Consultation
                      </Button>
                    ) : (
                      <Button 
                        onClick={() => handleOpenCompleteModal(currentActive)} 
                        className="py-2.5 px-6 flex-1 text-xs bg-emerald-650 hover:bg-emerald-700 active:bg-emerald-800 text-white border-0 shadow-md shadow-emerald-500/10 rounded-xl font-bold hover:-translate-y-0.5 active:translate-y-0"
                      >
                        <UserCheck size={14} />
                        <span>Complete Consultation</span>
                      </Button>
                    )}

                    <Button 
                      onClick={() => handleDelayToggle(currentActive)} 
                      variant="outline" 
                      className={`py-2.5 px-6 text-xs rounded-xl border transition-colors font-bold shadow-xs hover:-translate-y-0.5 active:translate-y-0 ${
                        currentActive.status === 'DOCTOR_RUNNING_LATE'
                          ? 'border-slate-200 text-slate-700 hover:bg-slate-50'
                          : 'border-amber-250 text-amber-700 hover:bg-amber-50'
                      }`}
                      loading={statusMutation.isPending}
                    >
                      {currentActive.status === 'DOCTOR_RUNNING_LATE' ? 'Clear Delay Warning' : 'Signal Delay Warning'}
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="text-center py-16 flex flex-col justify-center items-center h-full min-h-[290px] select-none bg-slate-50/20 border border-dashed border-slate-200 rounded-2xl">
                  <div className="w-14 h-14 bg-slate-50 border border-slate-100/75 rounded-2xl flex items-center justify-center text-slate-400 mb-4 shadow-xs animate-pulse">
                    <Clock size={24} />
                  </div>
                  <h3 className="text-base font-extrabold text-slate-805">Queue Empty</h3>
                  <p className="text-xs text-slate-400 font-semibold max-w-xs mt-2 leading-relaxed">
                    No active or waiting patient sessions registered for today.
                  </p>
                </div>
              )}
            </Card>
          </div>

          {/* Waiting Queue List */}
          <div className="lg:col-span-1">
            <Card className="min-h-[380px] flex flex-col h-full !p-5 rounded-3xl" hoverEffect={false}>
              <h3 className="text-xs font-bold text-slate-700 uppercase tracking-widest mb-4 pb-3.5 border-b border-slate-150/40 select-none">
                Waiting Room ({waitingPatients.length})
              </h3>
              
              <div className="flex-1 overflow-y-auto space-y-2.5 pr-1 max-h-[320px] scrollbar-thin">
                {waitingPatients.length > 0 ? (
                  waitingPatients.map((item, idx) => (
                    <div 
                      key={item._id} 
                      className={`p-3.5 border rounded-2xl flex items-center justify-between transition-all duration-200 ${
                        idx === 0 
                          ? 'bg-medical-blue-50/45 border-medical-blue-150/70 shadow-xs' 
                          : 'bg-slate-50/50 border-slate-100'
                      }`}
                    >
                      <div className="space-y-0.5">
                        <p className="font-extrabold text-slate-800 text-xs">{item.patientName}</p>
                        <span className="text-[9px] text-slate-400 font-bold uppercase tracking-wider block">Token Spot #{item.tokenNumber}</span>
                      </div>
                      {idx === 0 && (
                        <span className="text-[8px] font-black text-medical-blue-700 uppercase bg-white border border-medical-blue-150/60 px-2 py-0.5 rounded-lg select-none shadow-xs">
                          NEXT
                        </span>
                      )}
                    </div>
                  ))
                ) : (
                  <div className="text-center py-16 text-xs font-semibold text-slate-400 select-none bg-slate-50/30 border border-dashed border-slate-100 rounded-2xl">
                    No patients waiting.
                  </div>
                )}
              </div>
            </Card>
          </div>
        </div>
      )}

      {/* Write Prescription / Complete Consultation Modal */}
      <Modal
        isOpen={activeQueueItem !== null}
        onClose={() => setActiveQueueItem(null)}
        title="Consultation Summary Record"
      >
        {activeQueueItem && (
          <form onSubmit={handleCompleteSubmit} className="space-y-4 font-bold text-xs text-slate-650">
            <div className="border-b border-slate-100 pb-3 mb-3 select-none">
              <p className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest">PATIENT CONSULTATION</p>
              <p className="font-black text-sm text-medical-blue-700 mt-1">{activeQueueItem.patientName}</p>
            </div>

            {/* Diagnosis input */}
            <div className="space-y-1.5">
              <label className="block text-slate-700 uppercase tracking-wider text-[10px]">Diagnosis Summary</label>
              <input
                type="text"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                placeholder="e.g. Seasonal viral fever, Hypertension logs"
                value={diagnosis}
                onChange={(e) => setDiagnosis(e.target.value)}
                required
                disabled={completeMutation.isPending}
              />
            </div>

            {/* Prescription details */}
            <div className="space-y-1.5">
              <label className="block text-slate-700 uppercase tracking-wider text-[10px] flex items-center gap-1.5 select-none">
                <Pill size={14} className="text-medical-teal-500 flex-shrink-0 animate-pulse" /> 
                <span>Prescription Details (Rx)</span>
              </label>
              <textarea
                className="w-full px-4 py-2.5 rounded-2xl border border-slate-200 text-xs font-semibold font-mono leading-relaxed focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all resize-none shadow-inner bg-slate-50"
                rows={4}
                placeholder="Paracetamol 650mg - 1-0-1 after food - 5 Days&#10;Amoxicillin 500mg - 1-1-1 - 3 Days"
                value={prescription}
                onChange={(e) => setPrescription(e.target.value)}
                disabled={completeMutation.isPending}
              />
            </div>

            {/* Clinical Notes */}
            <div className="space-y-1.5">
              <label className="block text-slate-700 uppercase tracking-wider text-[10px] flex items-center gap-1.5 select-none">
                <FileText size={14} className="text-medical-blue-500 flex-shrink-0" /> 
                <span>Clinical Notes & Advisories</span>
              </label>
              <textarea
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-xs leading-relaxed focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all resize-none bg-white"
                rows={3}
                placeholder="Bed rest advised. Retain body temp log..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                disabled={completeMutation.isPending}
              />
            </div>

            {/* Follow up date */}
            <div className="space-y-1.5">
              <label className="block text-slate-700 uppercase tracking-wider text-[10px] flex items-center gap-1.5 select-none">
                <Calendar size={14} className="text-slate-400 flex-shrink-0" /> 
                <span>Follow-up Date (Optional)</span>
              </label>
              <input
                type="date"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200 bg-white"
                value={followUpDate}
                onChange={(e) => setFollowUpDate(e.target.value)}
                disabled={completeMutation.isPending}
              />
            </div>

            <div className="flex gap-3 pt-4">
              <Button type="button" onClick={() => setActiveQueueItem(null)} variant="outline" className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-xs">
                Cancel
              </Button>
              <Button 
                type="submit" 
                className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-md shadow-medical-blue-500/5 hover:-translate-y-0.5 active:translate-y-0" 
                loading={completeMutation.isPending}
              >
                Save & Complete
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};

export default DoctorQueue;

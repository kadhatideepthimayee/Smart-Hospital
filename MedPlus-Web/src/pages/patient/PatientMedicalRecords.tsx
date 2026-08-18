import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getPatientMedicalRecords } from '../../api/medicalRecords';
import { Card, Button, Skeleton, Modal } from '../../components/UI';
import { FileText, Calendar, User, Clipboard, Pill, AlertCircle } from 'lucide-react';
import { MedicalRecord } from '../../types';

const PatientMedicalRecords: React.FC = () => {
  const [selectedRecord, setSelectedRecord] = useState<MedicalRecord | null>(null);

  // Fetch patient medical records
  const { data: records = [], isLoading, error } = useQuery({
    queryKey: ['patientMedicalRecords'],
    queryFn: getPatientMedicalRecords,
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <p className="text-xs font-bold text-slate-400 select-none">
        View diagnoses, medical prescriptions, and follow-up consultation records filed by doctors.
      </p>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Skeleton height="170px" className="rounded-3xl" />
          <Skeleton height="170px" className="rounded-3xl" />
        </div>
      ) : error ? (
        <div className="bg-rose-50 border border-rose-100 p-4.5 rounded-2xl flex items-center gap-3 text-xs text-rose-700 font-semibold max-w-md mx-auto">
          <AlertCircle size={18} className="text-rose-600 flex-shrink-0" />
          <span>Failed to load medical logs. Please try again.</span>
        </div>
      ) : records.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {records.map(rec => (
            <Card key={rec._id} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
              <div>
                <div className="flex items-center gap-3.5 mb-4 select-none">
                  <div className="w-11 h-11 bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 rounded-xl flex items-center justify-center shadow-xs">
                    <FileText size={20} />
                  </div>
                  <div>
                    <h4 className="font-extrabold text-sm text-slate-900 leading-snug">{rec.diagnosis}</h4>
                    <span className="text-[9px] text-slate-450 font-extrabold uppercase tracking-widest block mt-0.5">DIAGNOSIS RECAP</span>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3.5 mb-4 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-500">
                  <div className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100/50">
                    <User size={13} className="text-slate-400 flex-shrink-0" />
                    <span className="truncate text-slate-700">Dr. {rec.doctorName ? rec.doctorName.split(' ').pop() : 'Practitioner'}</span>
                  </div>
                  <div className="flex items-center gap-2 bg-slate-50 p-2.5 rounded-xl border border-slate-100/50">
                    <Calendar size={13} className="text-slate-400 flex-shrink-0" />
                    <span className="truncate text-slate-700">
                      {new Date(rec.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                    </span>
                  </div>
                </div>
              </div>

              <Button 
                onClick={() => setSelectedRecord(rec)} 
                variant="outline" 
                className="py-2.5 text-xs mt-3 w-full rounded-xl font-bold shadow-xs flex items-center justify-center gap-1.5"
              >
                <span>View Full Prescription</span>
              </Button>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-14 h-14 bg-slate-50 text-slate-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-slate-100/70 shadow-xs">
            <FileText size={24} />
          </div>
          <h3 className="text-base font-extrabold text-slate-800">No Medical Records Found</h3>
          <p className="text-xs text-slate-400 font-semibold mt-2 max-w-xs mx-auto leading-relaxed">
            Consultation diagnoses and prescriptions will display here after your consultation.
          </p>
        </Card>
      )}

      {/* Prescription Detail Modal */}
      <Modal
        isOpen={selectedRecord !== null}
        onClose={() => setSelectedRecord(null)}
        title="Medical Prescription Card"
      >
        {selectedRecord && (
          <div className="space-y-5 text-xs font-bold text-slate-650">
            <div className="border-b border-slate-100 pb-4 select-none">
              <h4 className="text-[10px] text-slate-400 font-extrabold uppercase tracking-widest mb-1.5">Diagnosis</h4>
              <p className="font-black text-base text-slate-900 leading-snug">{selectedRecord.diagnosis}</p>
            </div>

            <div className="border-b border-slate-100 pb-4">
              <div className="flex items-center gap-2 mb-3 text-slate-700 select-none">
                <Pill size={14} className="text-medical-teal-600 flex-shrink-0 animate-pulse" />
                <h4 className="text-[10px] font-extrabold uppercase tracking-widest">Prescription Details (Rx)</h4>
              </div>
              <p className="text-xs text-slate-800 bg-slate-50 p-4 border border-slate-150 rounded-2xl whitespace-pre-line leading-relaxed font-semibold font-mono select-all shadow-inner">
                {selectedRecord.prescription || 'No medicines registered.'}
              </p>
            </div>

            {selectedRecord.notes && (
              <div className="border-b border-slate-100 pb-4">
                <div className="flex items-center gap-2 mb-3 text-slate-700 select-none">
                  <Clipboard size={14} className="text-medical-blue-600 flex-shrink-0" />
                  <h4 className="text-[10px] font-extrabold uppercase tracking-widest">Clinical Notes</h4>
                </div>
                <p className="text-xs text-slate-600 leading-relaxed bg-slate-50/50 p-4 border border-slate-100 rounded-2xl">
                  {selectedRecord.notes}
                </p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4 select-none">
              <div>
                <h4 className="text-[9px] text-slate-400 font-extrabold uppercase tracking-widest">Consultation Date</h4>
                <p className="text-xs font-extrabold text-slate-800 mt-1">
                  {new Date(selectedRecord.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
                </p>
              </div>
              {selectedRecord.followUpDate && (
                <div>
                  <h4 className="text-[9px] text-slate-400 font-extrabold uppercase tracking-widest">Follow-up Date</h4>
                  <p className="text-xs font-black text-medical-teal-650 mt-1">{selectedRecord.followUpDate}</p>
                </div>
              )}
            </div>

            <Button onClick={() => setSelectedRecord(null)} className="w-full mt-6 py-2.5 rounded-xl text-xs font-bold shadow-md">
              Close Prescription
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default PatientMedicalRecords;

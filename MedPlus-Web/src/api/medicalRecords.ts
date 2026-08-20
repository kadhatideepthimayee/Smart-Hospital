import { MedicalRecord } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const createMedicalRecord = async (recordData: {
  patientId: string;
  appointmentId: string;
  diagnosis: string;
  prescription: string;
  notes?: string;
  followUpDate?: string;
}): Promise<MedicalRecord> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/medical-records`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(recordData)
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to create medical record.');
  }

  const data = await res.json();
  return {
    _id: data.id,
    recordId: data.id,
    patientId: data.patientId,
    patientName: data.patientName || '',
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '',
    appointmentId: data.appointmentId || '',
    diagnosis: data.diagnosis || '',
    prescription: data.prescription || '',
    notes: data.notes || '',
    followUpDate: data.followUpDate || '',
    createdAt: data.createdAt || ''
  };
};

export const getPatientMedicalRecords = async (): Promise<MedicalRecord[]> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return [];

  const res = await fetch(`${API_BASE_URL}/medical-records/patient/${currentUid}`);
  if (!res.ok) throw new Error('Failed to fetch patient medical records.');
  const list = await res.json();

  return list.map((data: any) => ({
    _id: data.id,
    recordId: data.id,
    patientId: data.patientId || '',
    patientName: data.patientName || '',
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '',
    appointmentId: data.appointmentId || '',
    diagnosis: data.diagnosis || '',
    prescription: data.prescription || '',
    notes: data.notes || '',
    followUpDate: data.followUpDate || '',
    createdAt: data.createdAt || ''
  }));
};

export const getMedicalRecordById = async (id: string): Promise<MedicalRecord> => {
  const res = await fetch(`${API_BASE_URL}/medical-records/${id}`);
  if (!res.ok) throw new Error('Medical record not found');
  const data = await res.json();

  return {
    _id: data.id,
    recordId: data.id,
    patientId: data.patientId || '',
    patientName: data.patientName || '',
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '',
    appointmentId: data.appointmentId || '',
    diagnosis: data.diagnosis || '',
    prescription: data.prescription || '',
    notes: data.notes || '',
    followUpDate: data.followUpDate || '',
    createdAt: data.createdAt || ''
  };
};

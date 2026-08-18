import axiosInstance from './axiosInstance';
import { MedicalRecord } from '../types';

export const createMedicalRecord = async (recordData: {
  patientId: string;
  appointmentId: string;
  diagnosis: string;
  prescription: string;
  notes?: string;
  followUpDate?: string;
}): Promise<MedicalRecord> => {
  const response = await axiosInstance.post('/medical-records', recordData);
  return response.data;
};

export const getPatientMedicalRecords = async (): Promise<MedicalRecord[]> => {
  const response = await axiosInstance.get('/medical-records/patient');
  return response.data;
};

export const getMedicalRecordById = async (id: string): Promise<MedicalRecord> => {
  const response = await axiosInstance.get(`/medical-records/${id}`);
  return response.data;
};

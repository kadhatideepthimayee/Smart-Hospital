import { 
  collection, 
  doc, 
  getDoc, 
  getDocs, 
  query, 
  where, 
  addDoc, 
  Timestamp 
} from 'firebase/firestore';
import { auth, db } from '../lib/firebase';
import { MedicalRecord } from '../types';

export const createMedicalRecord = async (recordData: {
  patientId: string;
  appointmentId: string;
  diagnosis: string;
  prescription: string;
  notes?: string;
  followUpDate?: string;
}): Promise<MedicalRecord> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  // 1. Fetch appointment details
  const apptSnap = await getDoc(doc(db, 'appointments', recordData.appointmentId));
  if (!apptSnap.exists()) throw new Error('Appointment not found');
  const appointment = apptSnap.data();

  const createdAtStr = new Date().toISOString();

  // 2. Save Medical Record
  const newRecord = {
    patientId: recordData.patientId,
    patientName: appointment.patientName || 'Patient',
    doctorId: currentUid,
    doctorName: appointment.doctorName || 'Doctor',
    appointmentId: recordData.appointmentId,
    diagnosis: recordData.diagnosis,
    prescription: recordData.prescription,
    notes: recordData.notes || '',
    followUpDate: recordData.followUpDate || '',
    createdAt: createdAtStr
  };

  const docRef = await addDoc(collection(db, 'medical_records'), newRecord);

  // 3. Send notification to patient
  await addDoc(collection(db, 'notifications'), {
    userId: recordData.patientId,
    title: 'New Medical Record Available',
    message: `Dr. ${appointment.doctorName} has added a medical record for your consultation.`,
    type: 'MEDICAL_RECORD',
    isRead: false,
    timestamp: Timestamp.now()
  });

  return {
    _id: docRef.id,
    recordId: docRef.id,
    ...newRecord
  };
};

export const getPatientMedicalRecords = async (): Promise<MedicalRecord[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  const q = query(collection(db, 'medical_records'), where('patientId', '==', currentUid));
  const snapshot = await getDocs(q);

  const list = snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      _id: docSnap.id,
      recordId: docSnap.id,
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
  });
  
  // Sort descending by creation date
  return list.sort((a, b) => b.createdAt.localeCompare(a.createdAt));
};

export const getMedicalRecordById = async (id: string): Promise<MedicalRecord> => {
  const docSnap = await getDoc(doc(db, 'medical_records', id));
  if (!docSnap.exists()) throw new Error('Medical record not found');

  const data = docSnap.data();
  return {
    _id: docSnap.id,
    recordId: docSnap.id,
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

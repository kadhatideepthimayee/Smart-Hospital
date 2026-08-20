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
import { DoctorFeedback } from '../types';

export const submitFeedback = async (feedbackData: {
  doctorId: string;
  appointmentId: string;
  rating: number; // 1-5
  feedback?: string;
}): Promise<DoctorFeedback> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  const fbData = {
    doctorId: feedbackData.doctorId,
    patientId: currentUid,
    rating: feedbackData.rating,
    feedback: feedbackData.feedback || '',
    appointmentId: feedbackData.appointmentId,
    createdAt: Timestamp.now()
  };

  const docRef = await addDoc(collection(db, 'feedback'), fbData);
  return {
    id: docRef.id,
    ...fbData,
    createdAt: new Date().toISOString()
  };
};

export const getDoctorFeedback = async (doctorId: string): Promise<DoctorFeedback[]> => {
  const q = query(collection(db, 'feedback'), where('doctorId', '==', doctorId));
  const snapshot = await getDocs(q);

  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      id: docSnap.id,
      doctorId: data.doctorId || '',
      patientId: data.patientId || '',
      rating: data.rating || 5,
      feedback: data.feedback || '',
      appointmentId: data.appointmentId || '',
      createdAt: data.createdAt ? new Date(data.createdAt.seconds * 1000).toISOString() : ''
    };
  });
};

export const getFeedbackForAppointment = async (
  appointmentId: string
): Promise<{ exists: boolean; feedback?: DoctorFeedback }> => {
  const q = query(collection(db, 'feedback'), where('appointmentId', '==', appointmentId));
  const snapshot = await getDocs(q);

  if (snapshot.empty) {
    return { exists: false };
  }

  const docSnap = snapshot.docs[0];
  const data = docSnap.data();
  return {
    exists: true,
    feedback: {
      id: docSnap.id,
      doctorId: data.doctorId || '',
      patientId: data.patientId || '',
      rating: data.rating || 5,
      feedback: data.feedback || '',
      appointmentId: data.appointmentId || '',
      createdAt: data.createdAt ? new Date(data.createdAt.seconds * 1000).toISOString() : ''
    }
  };
};

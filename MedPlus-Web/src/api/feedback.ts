import { DoctorFeedback } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const submitFeedback = async (feedbackData: {
  doctorId: string;
  appointmentId: string;
  rating: number; // 1-5
  feedback?: string;
}): Promise<DoctorFeedback> => {
  const currentUid = localStorage.getItem('medplus_uid');
  const token = localStorage.getItem('medplus_token');
  if (!currentUid || !token) throw new Error('User not authenticated');

  const res = await fetch(`${API_BASE_URL}/feedback`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      doctorId: feedbackData.doctorId,
      patientId: currentUid,
      rating: feedbackData.rating,
      feedback: feedbackData.feedback || '',
      appointmentId: feedbackData.appointmentId
    })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to submit feedback.');
  }

  return await res.json();
};

export const getDoctorFeedback = async (doctorId: string): Promise<DoctorFeedback[]> => {
  const res = await fetch(`${API_BASE_URL}/feedback/doctor/${doctorId}`);
  if (!res.ok) throw new Error('Failed to fetch doctor feedback.');
  return await res.json();
};

export const getFeedbackForAppointment = async (
  appointmentId: string
): Promise<{ exists: boolean; feedback?: DoctorFeedback }> => {
  const res = await fetch(`${API_BASE_URL}/feedback/appointment/${appointmentId}`);
  if (!res.ok) throw new Error('Failed to check feedback for appointment.');
  return await res.json();
};

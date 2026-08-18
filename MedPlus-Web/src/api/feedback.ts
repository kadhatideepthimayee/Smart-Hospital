import axiosInstance from './axiosInstance';
import { DoctorFeedback } from '../types';

export const submitFeedback = async (feedbackData: {
  doctorId: string;
  appointmentId: string;
  rating: number; // 1-5
  feedback?: string;
}): Promise<DoctorFeedback> => {
  const response = await axiosInstance.post('/feedback', feedbackData);
  return response.data;
};

export const getDoctorFeedback = async (doctorId: string): Promise<DoctorFeedback[]> => {
  const response = await axiosInstance.get(`/feedback/doctor/${doctorId}`);
  return response.data;
};

export const getFeedbackForAppointment = async (
  appointmentId: string
): Promise<{ exists: boolean; feedback?: DoctorFeedback }> => {
  const response = await axiosInstance.get(`/feedback/appointment/${appointmentId}`);
  return response.data;
};

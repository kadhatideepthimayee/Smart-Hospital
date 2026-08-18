import axiosInstance from './axiosInstance';
import { DoctorProfile, QueueItem, User } from '../types';

export const getDoctorProfile = async (): Promise<DoctorProfile> => {
  const response = await axiosInstance.get('/doctors/profile');
  return response.data;
};

export const getDoctorProfileByUid = async (uid: string): Promise<DoctorProfile> => {
  const response = await axiosInstance.get(`/doctors/profile/${uid}`);
  return response.data;
};

export const setupDoctorProfile = async (profileData: Partial<DoctorProfile>): Promise<DoctorProfile> => {
  const response = await axiosInstance.post('/doctors/profile/setup', profileData);
  return response.data;
};

export const getVerifiedDoctors = async (): Promise<DoctorProfile[]> => {
  const response = await axiosInstance.get('/doctors/verified');
  return response.data;
};

export const updateAvailability = async (availabilityData: Partial<DoctorProfile>): Promise<DoctorProfile> => {
  const response = await axiosInstance.post('/doctors/availability', availabilityData);
  return response.data;
};

export const getDoctorQueue = async (date?: string): Promise<QueueItem[]> => {
  const url = date ? `/doctors/queue?date=${date}` : '/doctors/queue';
  const response = await axiosInstance.get(url);
  return response.data;
};

export const updateQueueStatus = async (queueId: string, newStatus: string): Promise<QueueItem> => {
  const response = await axiosInstance.put(`/doctors/queue/${queueId}`, { newStatus });
  return response.data;
};

export const getDoctorPatients = async (): Promise<User[]> => {
  const response = await axiosInstance.get('/doctors/patients');
  return response.data;
};

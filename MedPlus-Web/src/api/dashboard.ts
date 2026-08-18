import axiosInstance from './axiosInstance';
import { Notification, Activity } from '../types';

export interface LiveQueueData {
  isActive: boolean;
  queueNumber: string;
  currentServingToken: string;
  status: string;
  patientsAhead: number;
  estimatedWaitMinutes: number;
  crowdLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  department: string;
}

export const getNotifications = async (): Promise<Notification[]> => {
  const response = await axiosInstance.get('/dashboard/notifications');
  return response.data;
};

export const getUnreadNotificationsCount = async (): Promise<{ count: number }> => {
  const response = await axiosInstance.get('/dashboard/notifications/unread-count');
  return response.data;
};

export const markNotificationAsRead = async (id: string): Promise<Notification> => {
  const response = await axiosInstance.put(`/dashboard/notifications/${id}/read`);
  return response.data;
};

export const markAllNotificationsAsRead = async (): Promise<{ msg: string }> => {
  const response = await axiosInstance.put('/dashboard/notifications/read-all');
  return response.data;
};

export const deleteNotification = async (id: string): Promise<{ msg: string }> => {
  const response = await axiosInstance.delete(`/dashboard/notifications/${id}`);
  return response.data;
};

export const getUpcomingAppointment = async (): Promise<{
  appointmentId: string;
  doctorName: string;
  department: string;
  status: string;
  date: string;
  time: string;
} | null> => {
  const response = await axiosInstance.get('/dashboard/upcoming-appointment');
  return response.data;
};

export const getLiveQueueTracking = async (appointmentId?: string): Promise<LiveQueueData | null> => {
  const url = appointmentId ? `/dashboard/live-queue?appointmentId=${appointmentId}` : '/dashboard/live-queue';
  const response = await axiosInstance.get(url);
  return response.data;
};

export const getActivities = async (): Promise<Activity[]> => {
  const response = await axiosInstance.get('/dashboard/activities');
  return response.data;
};

export const logActivity = async (activity: {
  type: string;
  title: string;
  description: string;
}): Promise<Activity> => {
  const response = await axiosInstance.post('/dashboard/activity', activity);
  return response.data;
};

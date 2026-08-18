import axiosInstance from './axiosInstance';
import { DoctorProfile, Notification, User, Appointment } from '../types';

export const getPendingDoctors = async (status?: string): Promise<DoctorProfile[]> => {
  const url = status ? `/admin/doctors?status=${status}` : '/admin/doctors';
  const response = await axiosInstance.get(url);
  return response.data;
};

export const getAllDoctors = async (): Promise<DoctorProfile[]> => {
  const response = await axiosInstance.get('/admin/doctors/all');
  return response.data;
};

export const verifyDoctor = async (
  doctorId: string,
  newStatus: 'VERIFIED' | 'APPROVED' | 'REJECTED',
  rejectionReason?: string
): Promise<{ msg: string }> => {
  const response = await axiosInstance.post('/admin/verify-doctor', {
    doctorId,
    newStatus,
    rejectionReason,
  });
  return response.data;
};

export const getAdminNotifications = async (): Promise<Notification[]> => {
  const response = await axiosInstance.get('/admin/notifications');
  return response.data;
};

export const deleteAdminNotification = async (id: string): Promise<{ msg: string }> => {
  const response = await axiosInstance.delete(`/admin/notifications/${id}`);
  return response.data;
};

export const markAdminNotificationRead = async (id: string): Promise<Notification> => {
  const response = await axiosInstance.put(`/admin/notifications/${id}/read`);
  return response.data;
};

export const getAdminUnreadCount = async (): Promise<{ count: number }> => {
  const response = await axiosInstance.get('/admin/notifications/unread-count');
  return response.data;
};

export const getAdminPatients = async (): Promise<User[]> => {
  const response = await axiosInstance.get('/admin/patients');
  return response.data;
};

export const getAdminAppointments = async (): Promise<Appointment[]> => {
  const response = await axiosInstance.get('/admin/appointments');
  return response.data;
};

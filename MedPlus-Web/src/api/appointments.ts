import axiosInstance from './axiosInstance';
import { Appointment } from '../types';

export const bookAppointment = async (bookingData: {
  doctorId: string;
  doctorName: string;
  department: string;
  date: string;
  time: string;
  reason?: string;
}): Promise<Appointment> => {
  const response = await axiosInstance.post('/appointments', bookingData);
  return response.data;
};

export const getPatientAppointments = async (): Promise<Appointment[]> => {
  const response = await axiosInstance.get('/appointments/patient');
  return response.data;
};

export const getDoctorAppointments = async (): Promise<Appointment[]> => {
  const response = await axiosInstance.get('/appointments/doctor');
  return response.data;
};

export const getAppointmentsByDoctorId = async (doctorId: string): Promise<Appointment[]> => {
  const response = await axiosInstance.get(`/appointments/doctor/${doctorId}`);
  return response.data;
};

export const getAppointmentDetails = async (id: string): Promise<Appointment> => {
  const response = await axiosInstance.get(`/appointments/${id}`);
  return response.data;
};

export const cancelAppointment = async (id: string): Promise<Appointment> => {
  const response = await axiosInstance.post(`/appointments/${id}/cancel`);
  return response.data;
};

export const updateAppointmentStatus = async (id: string, status: string): Promise<Appointment> => {
  const response = await axiosInstance.put(`/appointments/${id}/status`, { status });
  return response.data;
};

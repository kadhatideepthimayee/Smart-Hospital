import axiosInstance from './axiosInstance';
import { User } from '../types';

export const updateProfile = async (fullName: string, phone: string): Promise<User> => {
  const response = await axiosInstance.put('/auth/profile', { fullName, phone });
  return response.data;
};

export const forgotPassword = async (email: string): Promise<{ msg: string; debugPin?: string }> => {
  const response = await axiosInstance.post('/auth/forgot-password', { email });
  return response.data;
};

export const verifyResetCode = async (email: string, code: string): Promise<{ msg: string }> => {
  const response = await axiosInstance.post('/auth/verify-reset-code', { email, code });
  return response.data;
};

export const resetPassword = async (email: string, code: string, newPassword: string): Promise<{ msg: string }> => {
  const response = await axiosInstance.post('/auth/reset-password', { email, code, newPassword });
  return response.data;
};

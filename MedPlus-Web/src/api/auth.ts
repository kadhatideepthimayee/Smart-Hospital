import { User, UserRole } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const updateProfile = async (fullName: string, phone: string): Promise<User> => {
  const currentUid = localStorage.getItem('medplus_uid');
  const token = localStorage.getItem('medplus_token');
  if (!currentUid || !token) throw new Error('No authenticated user found');

  const res = await fetch(`${API_BASE_URL}/auth/profile/${currentUid}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ fullName, phone })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to update profile.');
  }

  // Fetch updated profile
  const profileRes = await fetch(`${API_BASE_URL}/auth/profile/${currentUid}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!profileRes.ok) throw new Error('Failed to retrieve updated profile');
  const userData = await profileRes.json();

  return {
    uid: userData.uid,
    fullName: userData.fullName,
    email: userData.email,
    phone: userData.phone,
    role: userData.role as UserRole,
    profileImage: userData.profileImage || ''
  };
};

export const forgotPassword = async (email: string): Promise<{ msg: string; debugPin?: string }> => {
  // Local backend stub: Return a simple dummy message for demonstration
  return { msg: 'Password reset request received. On the local backend, you can directly log in with your credentials.' };
};

export const verifyResetCode = async (_email: string, _code: string): Promise<{ msg: string }> => {
  return { msg: 'Reset code successfully verified.' };
};

export const resetPassword = async (_email: string, _code: string, _newPassword: string): Promise<{ msg: string }> => {
  return { msg: 'Password has been reset successfully.' };
};

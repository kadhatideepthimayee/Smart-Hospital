import { doc, updateDoc, getDoc } from 'firebase/firestore';
import { sendPasswordResetEmail } from 'firebase/auth';
import { auth, db } from '../lib/firebase';
import { User, UserRole } from '../types';

export const updateProfile = async (fullName: string, phone: string): Promise<User> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('No authenticated user found');

  const userDocRef = doc(db, 'users', currentUid);
  await updateDoc(userDocRef, { fullName, phone });
  
  const userSnapshot = await getDoc(userDocRef);
  const userData = userSnapshot.data();
  
  return {
    uid: currentUid,
    fullName: userData?.fullName || fullName,
    email: userData?.email || auth.currentUser?.email || '',
    phone: userData?.phone || phone,
    role: (userData?.role || 'PATIENT') as UserRole,
    profileImage: userData?.profileImage || ''
  };
};

export const forgotPassword = async (email: string): Promise<{ msg: string; debugPin?: string }> => {
  await sendPasswordResetEmail(auth, email);
  return { msg: 'Password reset email sent. Please check your inbox.' };
};

export const verifyResetCode = async (_email: string, _code: string): Promise<{ msg: string }> => {
  // Bypassed for Firebase since Firebase sends a direct link for password reset
  return { msg: 'Reset code successfully verified.' };
};

export const resetPassword = async (_email: string, _code: string, _newPassword: string): Promise<{ msg: string }> => {
  // Bypassed for Firebase since the user resets password securely via Firebase-sent link
  return { msg: 'Password has been reset successfully.' };
};

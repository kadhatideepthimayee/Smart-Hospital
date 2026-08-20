import { 
  collection, 
  doc, 
  getDoc, 
  getDocs, 
  query, 
  where, 
  updateDoc, 
  deleteDoc, 
  addDoc, 
  serverTimestamp,
  Timestamp 
} from 'firebase/firestore';
import { db } from '../lib/firebase';
import { DoctorProfile, Notification, User, Appointment, UserRole } from '../types';

export const getPendingDoctors = async (status?: string): Promise<DoctorProfile[]> => {
  let q = query(collection(db, 'doctor_profiles'));
  if (status) {
    q = query(collection(db, 'doctor_profiles'), where('verificationStatus', '==', status));
  }
  
  const snapshot = await getDocs(q);
  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      uid: docSnap.id,
      fullName: data.fullName || '',
      email: data.email || '',
      phone: data.phone || '',
      specialization: data.specialization || '',
      experience: data.experience || 0,
      department: data.department || '',
      consultationFee: data.consultationFee || 0,
      clinicName: data.clinicName || '',
      clinicAddress: data.clinicAddress || '',
      bio: data.bio || '',
      verificationStatus: data.verificationStatus || 'PENDING',
      workingDays: data.workingDays || [],
      consultationStartTime: data.consultationStartTime || '09:00 AM',
      consultationEndTime: data.consultationEndTime || '05:00 PM',
      slotDuration: data.slotDuration || 15,
      profileImage: data.profileImage || ''
    };
  });
};

export const getAllDoctors = async (): Promise<DoctorProfile[]> => {
  const snapshot = await getDocs(collection(db, 'doctor_profiles'));
  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      uid: docSnap.id,
      fullName: data.fullName || '',
      email: data.email || '',
      phone: data.phone || '',
      specialization: data.specialization || '',
      experience: data.experience || 0,
      department: data.department || '',
      consultationFee: data.consultationFee || 0,
      clinicName: data.clinicName || '',
      clinicAddress: data.clinicAddress || '',
      bio: data.bio || '',
      verificationStatus: data.verificationStatus || 'PENDING',
      workingDays: data.workingDays || [],
      consultationStartTime: data.consultationStartTime || '09:00 AM',
      consultationEndTime: data.consultationEndTime || '05:00 PM',
      slotDuration: data.slotDuration || 15,
      profileImage: data.profileImage || ''
    };
  });
};

export const verifyDoctor = async (
  doctorId: string,
  newStatus: 'VERIFIED' | 'APPROVED' | 'REJECTED',
  rejectionReason?: string
): Promise<{ msg: string }> => {
  const docRef = doc(db, 'doctor_profiles', doctorId);
  
  const updates: Record<string, any> = {
    verificationStatus: newStatus,
    reviewedAt: serverTimestamp()
  };
  if (rejectionReason) {
    updates.rejectionReason = rejectionReason;
  }
  
  await updateDoc(docRef, updates);

  // Send doctor a notification
  const title = "Verification Status Updated";
  const message = newStatus === 'REJECTED'
    ? `Your professional profile was rejected. Reason: ${rejectionReason || "Invalid credentials"}`
    : `Congratulations! Your professional profile has been verified. You can now consult patients.`;

  await addDoc(collection(db, 'notifications'), {
    userId: doctorId,
    title,
    message,
    type: 'VERIFICATION',
    isRead: false,
    timestamp: Timestamp.now()
  });

  return { msg: 'Doctor verification status updated successfully.' };
};

export const getAdminNotifications = async (): Promise<Notification[]> => {
  const snapshot = await getDocs(collection(db, 'admin_notifications'));
  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      id: docSnap.id,
      userId: data.userId || 'ADMIN',
      title: data.title || '',
      message: data.message || '',
      type: data.type || 'GENERAL',
      isRead: data.isRead !== undefined ? data.isRead : false,
      timestamp: data.timestamp ? new Date(data.timestamp.seconds * 1000).toLocaleString() : ''
    };
  });
};

export const deleteAdminNotification = async (id: string): Promise<{ msg: string }> => {
  await deleteDoc(doc(db, 'admin_notifications', id));
  return { msg: 'Admin notification deleted.' };
};

export const markAdminNotificationRead = async (id: string): Promise<Notification> => {
  const docRef = doc(db, 'admin_notifications', id);
  await updateDoc(docRef, { isRead: true });
  
  const updatedSnap = await getDoc(docRef);
  const data = updatedSnap.data() || {};
  return {
    id,
    userId: data.userId || 'ADMIN',
    title: data.title || '',
    message: data.message || '',
    type: data.type || 'GENERAL',
    isRead: true,
    timestamp: data.timestamp ? new Date(data.timestamp.seconds * 1000).toLocaleString() : ''
  };
};

export const getAdminUnreadCount = async (): Promise<{ count: number }> => {
  const snapshot = await getDocs(
    query(collection(db, 'admin_notifications'), where('isRead', '==', false))
  );
  return { count: snapshot.size };
};

export const getAdminPatients = async (): Promise<User[]> => {
  const q = query(collection(db, 'users'), where('role', '==', 'PATIENT'));
  const snapshot = await getDocs(q);
  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      uid: docSnap.id,
      fullName: data.fullName || '',
      email: data.email || '',
      phone: data.phone || '',
      role: 'PATIENT' as UserRole,
      profileImage: data.profileImage || ''
    };
  });
};

export const getAdminAppointments = async (): Promise<Appointment[]> => {
  const snapshot = await getDocs(collection(db, 'appointments'));
  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      _id: docSnap.id,
      appointmentId: docSnap.id,
      patientId: data.patientId || '',
      patientName: data.patientName || '',
      doctorId: data.doctorId || '',
      doctorName: data.doctorName || '',
      department: data.department || '',
      date: data.date || '',
      time: data.time || '',
      status: data.status || 'UPCOMING',
      tokenNumber: data.tokenNumber || null,
      consultationStartedAt: data.consultationStartedAt || null,
      consultationCompletedAt: data.consultationCompletedAt || null
    };
  });
};

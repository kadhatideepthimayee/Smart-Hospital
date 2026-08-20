import { DoctorProfile, Notification, User, Appointment, UserRole } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const getPendingDoctors = async (status?: string): Promise<DoctorProfile[]> => {
  let url = `${API_BASE_URL}/admin/pending-doctors`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch pending doctors.');
  const list = await res.json();

  const formatted = list.map((data: any) => ({
    uid: data.uid,
    fullName: data.fullName || '',
    email: data.email || '',
    phone: data.phone || '',
    specialization: data.specialization || '',
    experience: data.experienceYears || 0,
    department: data.department || '',
    consultationFee: data.consultationFee || 0,
    clinicName: data.clinicName || 'MedPlus Clinic',
    clinicAddress: data.clinicAddress || 'Clinic Address',
    bio: data.bio || '',
    verificationStatus: data.verificationStatus || 'PENDING',
    workingDays: data.workingDays || [],
    consultationStartTime: data.consultationStartTime || '09:00 AM',
    consultationEndTime: data.consultationEndTime || '05:00 PM',
    slotDuration: data.slotDuration || 15,
    profileImage: data.profileImage || ''
  }));

  if (status) {
    return formatted.filter((doc: any) => doc.verificationStatus === status);
  }
  return formatted;
};

export const getAllDoctors = async (): Promise<DoctorProfile[]> => {
  const res = await fetch(`${API_BASE_URL}/admin/doctors`);
  if (!res.ok) throw new Error('Failed to fetch all doctors.');
  const list = await res.json();

  return list.map((data: any) => ({
    uid: data.uid,
    fullName: data.fullName || '',
    email: data.email || '',
    phone: data.phone || '',
    specialization: data.specialization || '',
    experience: data.experienceYears || 0,
    department: data.department || '',
    consultationFee: data.consultationFee || 0,
    clinicName: data.clinicName || 'MedPlus Clinic',
    clinicAddress: data.clinicAddress || 'Clinic Address',
    bio: data.bio || '',
    verificationStatus: data.verificationStatus || 'PENDING',
    workingDays: data.workingDays || [],
    consultationStartTime: data.consultationStartTime || '09:00 AM',
    consultationEndTime: data.consultationEndTime || '05:00 PM',
    slotDuration: data.slotDuration || 15,
    profileImage: data.profileImage || ''
  }));
};

export const verifyDoctor = async (
  doctorId: string,
  newStatus: 'VERIFIED' | 'APPROVED' | 'REJECTED',
  rejectionReason?: string
): Promise<{ msg: string }> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/admin/verify-doctor/${doctorId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      status: newStatus,
      rejectionReason: rejectionReason || '',
      reviewedBy: 'ADMIN'
    })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to verify doctor.');
  }

  return { msg: 'Doctor verification status updated successfully.' };
};

export const getAdminNotifications = async (): Promise<Notification[]> => {
  const res = await fetch(`${API_BASE_URL}/notifications/ADMIN`);
  if (!res.ok) throw new Error('Failed to fetch admin notifications.');
  const list = await res.json();

  return list.map((data: any) => ({
    id: data.id,
    userId: 'ADMIN',
    doctorId: data.doctorId || '',
    title: data.title || '',
    message: data.message || '',
    type: data.type || 'GENERAL',
    isRead: data.read === 1,
    timestamp: data.createdAt ? new Date(data.createdAt).toLocaleString() : ''
  }));
};

export const deleteAdminNotification = async (id: string): Promise<{ msg: string }> => {
  const token = localStorage.getItem('medplus_token');
  await fetch(`${API_BASE_URL}/notifications/${id}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  return { msg: 'Admin notification deleted.' };
};

export const markAdminNotificationRead = async (id: string): Promise<Notification> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/notifications/${id}/read`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (!res.ok) throw new Error('Failed to mark notification read');
  
  const list = await getAdminNotifications();
  const updated = list.find(n => n.id === id);
  if (!updated) throw new Error('Notification not found');
  return updated;
};

export const getAdminUnreadCount = async (): Promise<{ count: number }> => {
  const list = await getAdminNotifications();
  const unread = list.filter(n => !n.isRead);
  return { count: unread.length };
};

export const getAdminPatients = async (): Promise<User[]> => {
  const res = await fetch(`${API_BASE_URL}/admin/patients`);
  if (!res.ok) throw new Error('Failed to fetch admin patients.');
  const list = await res.json();

  return list.map((data: any) => ({
    uid: data.uid,
    fullName: data.fullName || '',
    email: data.email || '',
    phone: data.phone || '',
    role: 'PATIENT' as UserRole,
    profileImage: data.profileImage || ''
  }));
};

export const getAdminAppointments = async (): Promise<Appointment[]> => {
  const res = await fetch(`${API_BASE_URL}/admin/appointments`);
  if (!res.ok) throw new Error('Failed to fetch admin appointments.');
  const list = await res.json();

  return list.map((data: any) => ({
    _id: data.id,
    appointmentId: data.id,
    patientId: data.patientId || '',
    patientName: data.patientName || '',
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '',
    department: data.department || '',
    date: data.date || '',
    time: data.time || '',
    status: data.status || 'PENDING',
    tokenNumber: data.tokenNumber || null,
    consultationStartedAt: null,
    consultationCompletedAt: null
  }));
};

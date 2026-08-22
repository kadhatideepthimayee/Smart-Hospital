import { DoctorProfile, QueueItem, User, UserRole } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const getDoctorProfile = async (): Promise<DoctorProfile> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) throw new Error('User not authenticated');
  return getDoctorProfileByUid(currentUid);
};

export const getDoctorProfileByUid = async (uid: string): Promise<DoctorProfile> => {
  const res = await fetch(`${API_BASE_URL}/doctors/${uid}`);
  if (!res.ok) {
    throw new Error('Doctor profile not found');
  }
  const data = await res.json();
  return {
    uid: data.uid,
    fullName: data.fullName || '',
    email: data.email || '',
    phone: data.phone || '',
    specialization: data.specialization || '',
    experience: data.experienceYears || 0, // Match field name
    experienceYears: data.experienceYears || 0,
    qualification: data.qualification || '',
    registrationAuthority: data.registrationAuthority || '',
    registrationNumber: data.registrationNumber || '',
    registrationCertificateUrl: data.registrationCertificateUrl || '',
    verificationDocumentUrl: data.verificationDocumentUrl || '',
    department: data.department || '',
    consultationFee: data.consultationFee || 0,
    clinicName: data.clinicName || 'MedPlus Clinic',
    clinicAddress: data.clinicAddress || 'Clinic Address',
    bio: data.bio || '',
    verificationStatus: data.verificationStatus || 'PENDING',
    workingDays: data.workingDays || [],
    consultationStartTime: data.consultationStartTime || '09:00 AM',
    consultationEndTime: data.consultationEndTime || '05:00 PM',
    lunchStartTime: data.lunchStartTime || '',
    lunchEndTime: data.lunchEndTime || '',
    breakStartTime: data.breakStartTime || '',
    breakEndTime: data.breakEndTime || '',
    slotDuration: data.slotDuration || 15,
    profileImage: data.profileImage || ''
  };
};

export const setupDoctorProfile = async (profileData: Partial<DoctorProfile>): Promise<DoctorProfile> => {
  const currentUid = localStorage.getItem('medplus_uid');
  const token = localStorage.getItem('medplus_token');
  if (!currentUid || !token) throw new Error('User not authenticated');

  // Fetch standard profile details first to check
  let existingUser = { fullName: '', email: '', phone: '' };
  try {
    const userRes = await fetch(`${API_BASE_URL}/auth/profile/${currentUid}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (userRes.ok) {
      existingUser = await userRes.json();
    }
  } catch (e) {
    // Ignore
  }

  const fullProfile = {
    ...profileData,
    uid: currentUid,
    fullName: existingUser.fullName || profileData.fullName || '',
    email: existingUser.email || profileData.email || '',
    phone: existingUser.phone || profileData.phone || '',
    experienceYears: profileData.experienceYears !== undefined 
      ? profileData.experienceYears 
      : (profileData.experience !== undefined ? profileData.experience : undefined),
    verificationStatus: 'PENDING'
  };

  const res = await fetch(`${API_BASE_URL}/doctors/${currentUid}/profile`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(fullProfile)
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to setup doctor profile.');
  }

  return getDoctorProfileByUid(currentUid);
};

export const getVerifiedDoctors = async (): Promise<DoctorProfile[]> => {
  const res = await fetch(`${API_BASE_URL}/doctors`);
  if (!res.ok) throw new Error('Failed to fetch verified doctors.');
  const list = await res.json();

  return list.map((data: any) => ({
    uid: data.uid,
    fullName: data.fullName || '',
    email: data.email || '',
    phone: data.phone || '',
    specialization: data.specialization || '',
    experience: data.experienceYears || 0,
    experienceYears: data.experienceYears || 0,
    qualification: data.qualification || '',
    registrationAuthority: data.registrationAuthority || '',
    registrationNumber: data.registrationNumber || '',
    registrationCertificateUrl: data.registrationCertificateUrl || '',
    verificationDocumentUrl: data.verificationDocumentUrl || '',
    department: data.department || '',
    consultationFee: data.consultationFee || 0,
    clinicName: data.clinicName || 'MedPlus Clinic',
    clinicAddress: data.clinicAddress || 'Clinic Address',
    bio: data.bio || '',
    verificationStatus: data.verificationStatus || 'PENDING',
    workingDays: data.workingDays || [],
    consultationStartTime: data.consultationStartTime || '09:00 AM',
    consultationEndTime: data.consultationEndTime || '05:00 PM',
    lunchStartTime: data.lunchStartTime || '',
    lunchEndTime: data.lunchEndTime || '',
    breakStartTime: data.breakStartTime || '',
    breakEndTime: data.breakEndTime || '',
    slotDuration: data.slotDuration || 15,
    profileImage: data.profileImage || ''
  }));
};

export const updateAvailability = async (availabilityData: Partial<DoctorProfile>): Promise<DoctorProfile> => {
  const currentUid = localStorage.getItem('medplus_uid');
  const token = localStorage.getItem('medplus_token');
  if (!currentUid || !token) throw new Error('User not authenticated');

  const res = await fetch(`${API_BASE_URL}/doctors/${currentUid}/profile`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(availabilityData)
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to update availability.');
  }

  return getDoctorProfileByUid(currentUid);
};

export const getDoctorQueue = async (date?: string): Promise<QueueItem[]> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return [];

  let url = `${API_BASE_URL}/queue?doctorId=${currentUid}`;
  if (date) {
    url += `&date=${encodeURIComponent(date)}`;
  }

  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to fetch doctor queue.');
  return await res.json();
};

export const updateQueueStatus = async (queueId: string, newStatus: string): Promise<QueueItem> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/queue/${queueId}/status`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ status: newStatus })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to update queue status.');
  }

  return await res.json();
};

export const getDoctorPatients = async (): Promise<User[]> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return [];

  const res = await fetch(`${API_BASE_URL}/doctors/patients/${currentUid}`);
  if (!res.ok) throw new Error('Failed to fetch doctor patients.');
  const list = await res.json();

  return list.map((data: any) => ({
    uid: data.uid,
    fullName: data.fullName || '',
    email: data.email || '',
    phone: data.phone || '',
    role: (data.role || 'PATIENT') as UserRole,
    profileImage: data.profileImage || ''
  }));
};

import { 
  collection, 
  doc, 
  getDoc, 
  getDocs, 
  query, 
  where, 
  setDoc, 
  updateDoc 
} from 'firebase/firestore';
import { auth, db } from '../lib/firebase';
import { DoctorProfile, QueueItem, User, UserRole } from '../types';

export const getDoctorProfile = async (): Promise<DoctorProfile> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');
  return getDoctorProfileByUid(currentUid);
};

export const getDoctorProfileByUid = async (uid: string): Promise<DoctorProfile> => {
  const docRef = doc(db, 'doctor_profiles', uid);
  const docSnap = await getDoc(docRef);
  
  if (!docSnap.exists()) {
    throw new Error('Doctor profile not found');
  }

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
    lunchStartTime: data.lunchStartTime || '',
    lunchEndTime: data.lunchEndTime || '',
    breakStartTime: data.breakStartTime || '',
    breakEndTime: data.breakEndTime || '',
    slotDuration: data.slotDuration || 15,
    profileImage: data.profileImage || ''
  };
};

export const setupDoctorProfile = async (profileData: Partial<DoctorProfile>): Promise<DoctorProfile> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  // Fetch user name
  const userDoc = await getDoc(doc(db, 'users', currentUid));
  const userData = userDoc.data() || {};

  const fullProfile: Partial<DoctorProfile> = {
    ...profileData,
    uid: currentUid,
    fullName: userData.fullName || profileData.fullName || '',
    email: userData.email || profileData.email || '',
    phone: userData.phone || profileData.phone || '',
    verificationStatus: 'PENDING'
  };

  await setDoc(doc(db, 'doctor_profiles', currentUid), fullProfile, { merge: true });
  return getDoctorProfileByUid(currentUid);
};

export const getVerifiedDoctors = async (): Promise<DoctorProfile[]> => {
  const q = query(collection(db, 'doctor_profiles'), where('verificationStatus', '==', 'VERIFIED'));
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
      lunchStartTime: data.lunchStartTime || '',
      lunchEndTime: data.lunchEndTime || '',
      breakStartTime: data.breakStartTime || '',
      breakEndTime: data.breakEndTime || '',
      slotDuration: data.slotDuration || 15,
      profileImage: data.profileImage || ''
    };
  });
};

export const updateAvailability = async (availabilityData: Partial<DoctorProfile>): Promise<DoctorProfile> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  await setDoc(doc(db, 'doctor_profiles', currentUid), availabilityData, { merge: true });
  return getDoctorProfileByUid(currentUid);
};

export const getDoctorQueue = async (date?: string): Promise<QueueItem[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  let q = query(collection(db, 'queue'), where('doctorId', '==', currentUid));
  if (date) {
    q = query(collection(db, 'queue'), where('doctorId', '==', currentUid), where('date', '==', date));
  }
  
  const snapshot = await getDocs(q);
  return snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      _id: docSnap.id,
      queueId: docSnap.id,
      appointmentId: data.appointmentId || '',
      doctorId: data.doctorId || '',
      patientId: data.patientId || '',
      patientName: data.patientName || '',
      tokenNumber: data.tokenNumber || '',
      status: data.status || 'WAITING',
      department: data.department || '',
      date: data.date || '',
      isActive: data.isActive !== undefined ? data.isActive : true,
      estimatedWaitMinutes: data.estimatedWaitMinutes || 0
    };
  });
};

export const updateQueueStatus = async (queueId: string, newStatus: string): Promise<QueueItem> => {
  const docRef = doc(db, 'queue', queueId);
  const isActive = !(newStatus === 'COMPLETED' || newStatus === 'CANCELLED');
  
  await updateDoc(docRef, { 
    status: newStatus,
    isActive: isActive
  });

  const updatedSnap = await getDoc(docRef);
  const data = updatedSnap.data() || {};
  return {
    _id: docRef.id,
    queueId: docRef.id,
    appointmentId: data.appointmentId || '',
    doctorId: data.doctorId || '',
    patientId: data.patientId || '',
    patientName: data.patientName || '',
    tokenNumber: data.tokenNumber || '',
    status: newStatus,
    department: data.department || '',
    date: data.date || '',
    isActive: isActive,
    estimatedWaitMinutes: data.estimatedWaitMinutes || 0
  };
};

export const getDoctorPatients = async (): Promise<User[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  // Fetch doctor's appointments
  const apptsSnapshot = await getDocs(
    query(collection(db, 'appointments'), where('doctorId', '==', currentUid))
  );

  const patientIds = Array.from(new Set(apptsSnapshot.docs.map(docSnap => docSnap.data().patientId).filter(Boolean)));
  if (patientIds.length === 0) return [];

  const users: User[] = [];
  // Chunk queries by 10 (Firestore limit for `in` operator)
  for (let i = 0; i < patientIds.length; i += 10) {
    const chunk = patientIds.slice(i, i + 10);
    const usersSnapshot = await getDocs(
      query(collection(db, 'users'), where('uid', 'in', chunk))
    );
    usersSnapshot.docs.forEach(docSnap => {
      const data = docSnap.data();
      users.push({
        uid: docSnap.id,
        fullName: data.fullName || '',
        email: data.email || '',
        phone: data.phone || '',
        role: (data.role || 'PATIENT') as UserRole,
        profileImage: data.profileImage || ''
      });
    });
  }

  return users;
};

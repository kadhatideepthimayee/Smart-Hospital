export type UserRole = 'ADMIN' | 'DOCTOR' | 'PATIENT';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface User {
  uid: string;
  fullName: string;
  email: string;
  phone: string;
  role: UserRole;
  profileImage?: string;
  status: UserStatus;
}

export type VerificationStatus = 'DRAFT' | 'PENDING' | 'VERIFIED' | 'APPROVED' | 'REJECTED';

export interface DoctorProfile {
  _id?: string;
  uid: string;
  fullName: string;
  email: string;
  phone: string;
  qualification?: string;
  department?: string;
  specialization?: string;
  experienceYears?: number;
  experience?: number;
  clinicName?: string;
  clinicAddress?: string;
  registrationAuthority?: string;
  registrationNumber?: string;
  consultationFee?: number;
  bio?: string;
  profileImage?: string;
  registrationCertificateUrl?: string;
  verificationDocumentUrl?: string;
  workingDays?: string[];
  consultationStartTime?: string;
  consultationEndTime?: string;
  lunchStartTime?: string;
  lunchEndTime?: string;
  breakStartTime?: string;
  breakEndTime?: string;
  slotDuration?: number;
  verificationStatus: VerificationStatus;
  submittedAt?: string;
  reviewedAt?: string;
  reviewedBy?: string;
  rejectionReason?: string;
}

export type AppointmentStatus = 'UPCOMING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Appointment {
  _id: string;
  patientId: string;
  patientName: string;
  doctorId: string;
  doctorName: string;
  department: string;
  date: string; // YYYY-MM-DD
  time: string; // HH:MM AM/PM
  status: AppointmentStatus;
  tokenNumber: string;
  timestamp: string;
  consultationStartedAt?: string;
  consultationCompletedAt?: string;
  reason?: string;
  createdAt?: string;
}

export type QueueStatus = 'WAITING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'DOCTOR_RUNNING_LATE';

export interface QueueItem {
  _id: string;
  appointmentId: string;
  doctorId: string;
  patientId: string;
  patientName: string;
  tokenNumber: string;
  status: QueueStatus;
  department: string;
  date: string;
  isActive: boolean;
  estimatedWaitMinutes: number;
}

export interface MedicalRecord {
  _id: string;
  patientId: string;
  patientName: string;
  doctorId: string;
  doctorName: string;
  appointmentId: string;
  diagnosis: string;
  prescription: string;
  notes?: string;
  followUpDate?: string;
  createdAt: string;
}

export interface Notification {
  _id: string;
  id?: string; // mapping convenience
  userId?: string;
  doctorId?: string;
  title: string;
  message: string;
  type: 'APPOINTMENT' | 'QUEUE' | 'VERIFICATION' | 'DOCTOR_VERIFICATION' | 'FEEDBACK' | 'MEDICAL_RECORD';
  isRead: boolean;
  timestamp: string;
}

export interface DoctorFeedback {
  _id: string;
  doctorId: string;
  patientId: string;
  patientName: string;
  rating: number; // 1-5
  feedback?: string;
  appointmentId: string;
  createdAt?: string;
}

export interface Activity {
  _id: string;
  id?: string;
  userId: string;
  type: string;
  title: string;
  description: string;
  timestamp: string;
}

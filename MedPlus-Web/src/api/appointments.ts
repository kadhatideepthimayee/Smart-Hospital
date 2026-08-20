import { Appointment } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const bookAppointment = async (bookingData: {
  doctorId: string;
  doctorName: string;
  department: string;
  date: string;
  time: string;
  reason?: string;
}): Promise<Appointment> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) throw new Error('User not authenticated');

  const res = await fetch(`${API_BASE_URL}/appointments/book`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      patientId: currentUid,
      doctorId: bookingData.doctorId,
      doctorName: bookingData.doctorName,
      department: bookingData.department,
      date: bookingData.date,
      time: bookingData.time,
      reason: bookingData.reason || ''
    })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to book appointment.');
  }

  const data = await res.json();
  return {
    _id: data.id,
    appointmentId: data.id,
    patientId: data.patientId,
    patientName: data.patientName,
    doctorId: data.doctorId,
    doctorName: data.doctorName,
    department: data.department,
    date: data.date,
    time: data.time,
    status: data.status,
    tokenNumber: data.tokenNumber,
    consultationStartedAt: null,
    consultationCompletedAt: null
  };
};

export const getPatientAppointments = async (): Promise<Appointment[]> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return [];

  const res = await fetch(`${API_BASE_URL}/appointments/patient/${currentUid}`);
  if (!res.ok) throw new Error('Failed to fetch patient appointments.');
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

export const getDoctorAppointments = async (): Promise<Appointment[]> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return [];

  const res = await fetch(`${API_BASE_URL}/appointments/doctor/${currentUid}`);
  if (!res.ok) throw new Error('Failed to fetch doctor appointments.');
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

export const getAppointmentsByDoctorId = async (doctorId: string): Promise<Appointment[]> => {
  const res = await fetch(`${API_BASE_URL}/appointments/doctor/${doctorId}`);
  if (!res.ok) throw new Error('Failed to fetch doctor appointments.');
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

export const getAppointmentDetails = async (id: string): Promise<Appointment> => {
  const res = await fetch(`${API_BASE_URL}/appointments/${id}`);
  if (!res.ok) throw new Error('Failed to fetch appointment details.');
  const data = await res.json();

  return {
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
  };
};

export const cancelAppointment = async (id: string): Promise<Appointment> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/appointments/${id}/status`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ status: 'CANCELLED' })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to cancel appointment.');
  }

  return getAppointmentDetails(id);
};

export const updateAppointmentStatus = async (id: string, status: string): Promise<Appointment> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/appointments/${id}/status`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ status })
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to update appointment status.');
  }

  return getAppointmentDetails(id);
};

export const rescheduleAppointment = async (rescheduleData: {
  appointmentId: string;
  doctorId: string;
  doctorName: string;
  department: string;
  date: string;
  time: string;
  reason?: string;
}): Promise<Appointment> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/appointments/reschedule`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(rescheduleData)
  });

  if (!res.ok) {
    const errData = await res.json();
    throw new Error(errData.error || 'Failed to reschedule appointment.');
  }

  const data = await res.json();
  return {
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
  };
};

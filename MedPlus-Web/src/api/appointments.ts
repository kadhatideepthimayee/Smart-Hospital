import { 
  collection, 
  doc, 
  addDoc, 
  getDoc, 
  getDocs, 
  query, 
  where, 
  updateDoc, 
  serverTimestamp,
  Timestamp 
} from 'firebase/firestore';
import { auth, db } from '../lib/firebase';
import { Appointment } from '../types';

// Helper to parse time string
const timeToMinutes = (timeStr: string) => {
  if (!timeStr) return 0;
  const cleanTime = timeStr.trim();
  const timeParts = cleanTime.split(" ");
  const hm = timeParts[0].split(":");
  let hour = parseInt(hm[0]);
  const minute = parseInt(hm[1]);
  if (timeParts.length > 1) {
    const ampm = timeParts[1].toUpperCase();
    if (ampm === "PM" && hour < 12) hour += 12;
    if (ampm === "AM" && hour === 12) hour = 0;
  }
  return hour * 60 + minute;
};

export const bookAppointment = async (bookingData: {
  doctorId: string;
  doctorName: string;
  department: string;
  date: string;
  time: string;
  reason?: string;
}): Promise<Appointment> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  // 1. Fetch patient profile to get full name
  const patientDoc = await getDoc(doc(db, 'users', currentUid));
  const patientName = patientDoc.exists() ? patientDoc.data().fullName || 'Patient' : 'Patient';

  // 2. Determine token number based on count of appointments for that doctor on that date
  const doctorId = bookingData.doctorId;
  const date = bookingData.date;
  const time = bookingData.time;

  const apptsSnapshot = await getDocs(
    query(
      collection(db, 'appointments'), 
      where('doctorId', '==', doctorId), 
      where('date', '==', date)
    )
  );

  // Fetch doctor profile to get consultationStartTime and slotDuration
  const doctorDoc = await getDoc(doc(db, 'doctor_profiles', doctorId));
  const doctorData = doctorDoc.data();
  const startTimeStr = doctorData?.consultationStartTime || '09:00';
  const slotDuration = doctorData?.slotDuration || 15;

  const existingBookings = apptsSnapshot.docs.filter(doc => doc.data().time === time).length;

  const diffMinutes = timeToMinutes(time) - timeToMinutes(startTimeStr);
  const baseToken = Math.max(1, Math.floor(diffMinutes / slotDuration));
  const tokenNumber = (baseToken + existingBookings).toString();

  // Combine Date & Time into firestore Timestamp
  let appointmentTimestamp = new Date();
  try {
    const dateParts = date.split(' ');
    const timeParts = time.split(' ');
    if (dateParts.length === 3 && timeParts.length === 2) {
      const monthStr = dateParts[0];
      const dayStr = dateParts[1].replace(',', '');
      const yearStr = dateParts[2];
      
      const hm = timeParts[0].split(':');
      let hours = parseInt(hm[0]);
      const minutes = parseInt(hm[1]);
      const ampm = timeParts[1].toUpperCase();

      if (ampm === 'PM' && hours < 12) hours += 12;
      if (ampm === 'AM' && hours === 12) hours = 0;

      const months: { [key: string]: number } = {
        Jan: 0, Feb: 1, Mar: 2, Apr: 3, May: 4, Jun: 5,
        Jul: 6, Aug: 7, Sep: 8, Oct: 9, Nov: 10, Dec: 11
      };
      const monthIndex = months[monthStr] !== undefined ? months[monthStr] : 0;
      appointmentTimestamp = new Date(parseInt(yearStr), monthIndex, parseInt(dayStr), hours, minutes);
    } else {
      appointmentTimestamp = new Date(`${date}T${time}`);
    }
    if (isNaN(appointmentTimestamp.getTime())) {
      appointmentTimestamp = new Date();
    }
  } catch (e) {
    appointmentTimestamp = new Date();
  }

  // 3. Save Appointment
  const newApptData = {
    patientId: currentUid,
    patientName,
    doctorId,
    doctorName: bookingData.doctorName,
    department: bookingData.department,
    date,
    time,
    status: 'UPCOMING',
    tokenNumber,
    timestamp: Timestamp.fromDate(appointmentTimestamp),
    createdAt: Timestamp.now()
  };

  const apptRef = await addDoc(collection(db, 'appointments'), newApptData);

  // 4. Save QueueItem
  await addDoc(collection(db, 'queue'), {
    appointmentId: apptRef.id,
    doctorId,
    patientId: currentUid,
    patientName,
    tokenNumber,
    status: 'WAITING',
    department: bookingData.department,
    date,
    isActive: true,
    estimatedWaitMinutes: 0,
    timestamp: Timestamp.now()
  });

  // 5. Send notifications
  // For Patient
  await addDoc(collection(db, 'notifications'), {
    userId: currentUid,
    title: 'Appointment Booked',
    message: `Your appointment with Dr. ${bookingData.doctorName} on ${date} at ${time} is confirmed. Token #${tokenNumber}.`,
    type: 'APPOINTMENT',
    isRead: false,
    timestamp: Timestamp.now()
  });

  // For Doctor
  await addDoc(collection(db, 'notifications'), {
    userId: doctorId,
    title: 'New Appointment Booked',
    message: `${patientName} has booked an appointment for ${date} at ${time}.`,
    type: 'APPOINTMENT',
    isRead: false,
    timestamp: Timestamp.now()
  });

  // 6. Log activity
  const timestampStr = new Date().toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    hour12: true
  });
  
  await addDoc(collection(db, 'activities'), {
    userId: currentUid,
    type: 'APPOINTMENT',
    title: 'Appointment Booked',
    description: `Booked consultation with Dr. ${bookingData.doctorName} for ${date} at ${time}`,
    timestamp: timestampStr
  });

  return {
    _id: apptRef.id,
    appointmentId: apptRef.id,
    patientId: currentUid,
    patientName,
    doctorId,
    doctorName: bookingData.doctorName,
    department: bookingData.department,
    date,
    time,
    status: 'UPCOMING',
    tokenNumber,
    consultationStartedAt: null,
    consultationCompletedAt: null
  };
};

export const getPatientAppointments = async (): Promise<Appointment[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  const q = query(collection(db, 'appointments'), where('patientId', '==', currentUid));
  const snapshot = await getDocs(q);

  return snapshot.docs.map(doc => {
    const data = doc.data();
    return {
      _id: doc.id,
      appointmentId: doc.id,
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

export const getDoctorAppointments = async (): Promise<Appointment[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  const q = query(collection(db, 'appointments'), where('doctorId', '==', currentUid));
  const snapshot = await getDocs(q);

  return snapshot.docs.map(doc => {
    const data = doc.data();
    return {
      _id: doc.id,
      appointmentId: doc.id,
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

export const getAppointmentsByDoctorId = async (doctorId: string): Promise<Appointment[]> => {
  const q = query(collection(db, 'appointments'), where('doctorId', '==', doctorId));
  const snapshot = await getDocs(q);

  return snapshot.docs.map(doc => {
    const data = doc.data();
    return {
      _id: doc.id,
      appointmentId: doc.id,
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

export const getAppointmentDetails = async (id: string): Promise<Appointment> => {
  const docSnap = await getDoc(doc(db, 'appointments', id));
  if (!docSnap.exists()) throw new Error('Appointment not found');

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
};

export const cancelAppointment = async (id: string): Promise<Appointment> => {
  const docRef = doc(db, 'appointments', id);
  await updateDoc(docRef, { status: 'CANCELLED' });

  // Update associated queue items to cancel them too
  const qSnapshot = await getDocs(query(collection(db, 'queue'), where('appointmentId', '==', id)));
  for (const queueDoc of qSnapshot.docs) {
    await updateDoc(doc(db, 'queue', queueDoc.id), { status: 'CANCELLED', isActive: false });
  }

  const updatedSnap = await getDoc(docRef);
  const data = updatedSnap.data() || {};
  return {
    _id: id,
    appointmentId: id,
    patientId: data.patientId || '',
    patientName: data.patientName || '',
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '',
    department: data.department || '',
    date: data.date || '',
    time: data.time || '',
    status: 'CANCELLED',
    tokenNumber: data.tokenNumber || null,
    consultationStartedAt: data.consultationStartedAt || null,
    consultationCompletedAt: data.consultationCompletedAt || null
  };
};

export const updateAppointmentStatus = async (id: string, status: string): Promise<Appointment> => {
  const docRef = doc(db, 'appointments', id);
  await updateDoc(docRef, { status });

  const updatedSnap = await getDoc(docRef);
  const data = updatedSnap.data() || {};
  return {
    _id: id,
    appointmentId: id,
    patientId: data.patientId || '',
    patientName: data.patientName || '',
    doctorId: data.doctorId || '',
    doctorName: data.doctorName || '',
    department: data.department || '',
    date: data.date || '',
    time: data.time || '',
    status,
    tokenNumber: data.tokenNumber || null,
    consultationStartedAt: data.consultationStartedAt || null,
    consultationCompletedAt: data.consultationCompletedAt || null
  };
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
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  // 1. Fetch patient profile to get full name
  const patientDoc = await getDoc(doc(db, 'users', currentUid));
  const patientName = patientDoc.exists() ? patientDoc.data().fullName || 'Patient' : 'Patient';

  const { appointmentId, doctorId, date, time } = rescheduleData;

  // 2. Count existing bookings at the new slot
  const apptsSnapshot = await getDocs(
    query(
      collection(db, 'appointments'), 
      where('doctorId', '==', doctorId), 
      where('date', '==', date)
    )
  );
  
  // Fetch doctor profile to get consultationStartTime and slotDuration
  const doctorDoc = await getDoc(doc(db, 'doctor_profiles', doctorId));
  const doctorData = doctorDoc.data();
  const startTimeStr = doctorData?.consultationStartTime || '09:00';
  const slotDuration = doctorData?.slotDuration || 15;

  const existingBookings = apptsSnapshot.docs.filter(doc => doc.data().time === time).length;

  const diffMinutes = timeToMinutes(time) - timeToMinutes(startTimeStr);
  const baseToken = Math.max(1, Math.floor(diffMinutes / slotDuration));
  const tokenNumber = (baseToken + existingBookings).toString();

  // Combine Date & Time into firestore Timestamp
  let appointmentTimestamp = new Date();
  try {
    const dateParts = date.split(' ');
    const timeParts = time.split(' ');
    if (dateParts.length === 3 && timeParts.length === 2) {
      const monthStr = dateParts[0];
      const dayStr = dateParts[1].replace(',', '');
      const yearStr = dateParts[2];
      
      const hm = timeParts[0].split(':');
      let hours = parseInt(hm[0]);
      const minutes = parseInt(hm[1]);
      const ampm = timeParts[1].toUpperCase();

      if (ampm === 'PM' && hours < 12) hours += 12;
      if (ampm === 'AM' && hours === 12) hours = 0;

      const months: { [key: string]: number } = {
        Jan: 0, Feb: 1, Mar: 2, Apr: 3, May: 4, Jun: 5,
        Jul: 6, Aug: 7, Sep: 8, Oct: 9, Nov: 10, Dec: 11
      };
      const monthIndex = months[monthStr] !== undefined ? months[monthStr] : 0;
      appointmentTimestamp = new Date(parseInt(yearStr), monthIndex, parseInt(dayStr), hours, minutes);
    } else {
      appointmentTimestamp = new Date(`${date}T${time}`);
    }
    if (isNaN(appointmentTimestamp.getTime())) {
      appointmentTimestamp = new Date();
    }
  } catch (e) {
    appointmentTimestamp = new Date();
  }

  // 3. Update Appointment
  const apptRef = doc(db, 'appointments', appointmentId);
  const updatedApptData = {
    patientId: currentUid,
    patientName,
    doctorId,
    doctorName: rescheduleData.doctorName,
    department: rescheduleData.department,
    date,
    time,
    status: 'UPCOMING',
    tokenNumber,
    timestamp: Timestamp.fromDate(appointmentTimestamp)
  };
  await updateDoc(apptRef, updatedApptData);

  // 4. Find existing queue item or create new
  const qSnapshot = await getDocs(
    query(collection(db, 'queue'), where('appointmentId', '==', appointmentId))
  );

  if (!qSnapshot.empty) {
    const queueDocId = qSnapshot.docs[0].id;
    await updateDoc(doc(db, 'queue', queueDocId), {
      date,
      tokenNumber,
      status: 'WAITING',
      isActive: true,
      timestamp: Timestamp.now()
    });
  } else {
    await addDoc(collection(db, 'queue'), {
      appointmentId,
      doctorId,
      patientId: currentUid,
      patientName,
      tokenNumber,
      status: 'WAITING',
      department: rescheduleData.department,
      date,
      isActive: true,
      estimatedWaitMinutes: 0,
      timestamp: Timestamp.now()
    });
  }

  // 5. Send notifications
  // For Patient
  await addDoc(collection(db, 'notifications'), {
    userId: currentUid,
    title: 'Appointment Rescheduled',
    message: `Your appointment with Dr. ${rescheduleData.doctorName} has been rescheduled to ${date} at ${time}. Token #${tokenNumber}.`,
    type: 'APPOINTMENT',
    isRead: false,
    timestamp: Timestamp.now()
  });

  // For Doctor
  await addDoc(collection(db, 'notifications'), {
    userId: doctorId,
    title: 'Appointment Rescheduled by Patient',
    message: `${patientName} has rescheduled their appointment to ${date} at ${time}.`,
    type: 'APPOINTMENT',
    isRead: false,
    timestamp: Timestamp.now()
  });

  // 6. Log activity
  const timestampStr = new Date().toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    hour12: true
  });
  
  await addDoc(collection(db, 'activities'), {
    userId: currentUid,
    type: 'APPOINTMENT',
    title: 'Appointment Rescheduled',
    description: `Rescheduled consultation with Dr. ${rescheduleData.doctorName} for ${date} at ${time}`,
    timestamp: timestampStr
  });

  return {
    _id: appointmentId,
    appointmentId,
    patientId: currentUid,
    patientName,
    doctorId,
    doctorName: rescheduleData.doctorName,
    department: rescheduleData.department,
    date,
    time,
    status: 'UPCOMING',
    tokenNumber,
    consultationStartedAt: null,
    consultationCompletedAt: null
  };
};

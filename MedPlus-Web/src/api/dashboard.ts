import { 
  collection, 
  doc, 
  getDoc, 
  getDocs, 
  query, 
  where, 
  orderBy, 
  limit, 
  updateDoc, 
  deleteDoc, 
  addDoc, 
  writeBatch,
  serverTimestamp,
  Timestamp 
} from 'firebase/firestore';
import { auth, db } from '../lib/firebase';
import { Notification, Activity } from '../types';

export interface LiveQueueData {
  isActive: boolean;
  queueNumber: string;
  currentServingToken: string;
  status: string;
  patientsAhead: number;
  estimatedWaitMinutes: number;
  crowdLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  department: string;
}

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

export const getNotifications = async (): Promise<Notification[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  const q = query(
    collection(db, 'notifications'), 
    where('userId', '==', currentUid)
  );
  
  const snapshot = await getDocs(q);
  const list = snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      id: docSnap.id,
      userId: data.userId || '',
      title: data.title || '',
      message: data.message || '',
      type: data.type || 'GENERAL',
      isRead: data.isRead !== undefined ? data.isRead : (data.read || false),
      timestamp: data.timestamp ? new Date(data.timestamp.seconds * 1000).toLocaleString() : ''
    };
  });
  
  // Sort descending by timestamp locally
  return list.sort((a, b) => b.timestamp.localeCompare(a.timestamp));
};

export const getUnreadNotificationsCount = async (): Promise<{ count: number }> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return { count: 0 };

  const snapshot = await getDocs(
    query(
      collection(db, 'notifications'), 
      where('userId', '==', currentUid), 
      where('isRead', '==', false)
    )
  );
  return { count: snapshot.size };
};

export const markNotificationAsRead = async (id: string): Promise<Notification> => {
  const docRef = doc(db, 'notifications', id);
  await updateDoc(docRef, { isRead: true, read: true });

  const updatedSnap = await getDoc(docRef);
  const data = updatedSnap.data() || {};
  return {
    id,
    userId: data.userId || '',
    title: data.title || '',
    message: data.message || '',
    type: data.type || 'GENERAL',
    isRead: true,
    timestamp: data.timestamp ? new Date(data.timestamp.seconds * 1000).toLocaleString() : ''
  };
};

export const markAllNotificationsAsRead = async (): Promise<{ msg: string }> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return { msg: 'User not authenticated' };

  const snapshot = await getDocs(
    query(
      collection(db, 'notifications'), 
      where('userId', '==', currentUid), 
      where('isRead', '==', false)
    )
  );

  const batch = writeBatch(db);
  snapshot.docs.forEach(docSnap => {
    batch.update(docSnap.ref, { isRead: true, read: true });
  });
  await batch.commit();

  return { msg: 'All notifications marked as read' };
};

export const deleteNotification = async (id: string): Promise<{ msg: string }> => {
  await deleteDoc(doc(db, 'notifications', id));
  return { msg: 'Notification deleted' };
};

export const getUpcomingAppointment = async (): Promise<{
  appointmentId: string;
  doctorName: string;
  department: string;
  status: string;
  date: string;
  time: string;
} | null> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return null;

  const snapshot = await getDocs(
    query(
      collection(db, 'appointments'), 
      where('patientId', '==', currentUid)
    )
  );

  const appointments = snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      appointmentId: docSnap.id,
      doctorName: data.doctorName || '',
      department: data.department || '',
      status: data.status || 'UPCOMING',
      date: data.date || '',
      time: data.time || ''
    };
  }).filter(appt => appt.status === 'UPCOMING' || appt.status === 'IN_PROGRESS');

  if (appointments.length === 0) return null;

  // Sort chronologically
  appointments.sort((a, b) => {
    return (a.date + ' ' + a.time).localeCompare(b.date + ' ' + b.time);
  });

  return appointments[0];
};

export const getLiveQueueTracking = async (appointmentId?: string): Promise<LiveQueueData | null> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return null;

  let activeQueueItem: any = null;

  if (appointmentId) {
    const snapshot = await getDocs(
      query(collection(db, 'queue'), where('appointmentId', '==', appointmentId))
    );
    if (!snapshot.empty) {
      activeQueueItem = { id: snapshot.docs[0].id, ...snapshot.docs[0].data() };
    }
  } else {
    // Find next upcoming appointment
    const upcomingAppt = await getUpcomingAppointment();
    if (upcomingAppt) {
      const snapshot = await getDocs(
        query(
          collection(db, 'queue'), 
          where('appointmentId', '==', upcomingAppt.appointmentId), 
          where('isActive', '==', true)
        )
      );
      if (!snapshot.empty) {
        activeQueueItem = { id: snapshot.docs[0].id, ...snapshot.docs[0].data() };
      }
    }

    if (!activeQueueItem) {
      const snapshot = await getDocs(
        query(
          collection(db, 'queue'), 
          where('patientId', '==', currentUid), 
          where('isActive', '==', true)
        )
      );
      if (!snapshot.empty) {
        activeQueueItem = { id: snapshot.docs[0].id, ...snapshot.docs[0].data() };
      }
    }
  }

  if (!activeQueueItem) return null;

  const doctorId = activeQueueItem.doctorId;
  const date = activeQueueItem.date;

  // Fetch doctor profile
  const doctorDoc = await getDoc(doc(db, 'doctor_profiles', doctorId));
  const defaultSlotDuration = doctorDoc.exists() ? doctorDoc.data().slotDuration || 15 : 15;
  const consultationStartTimeStr = doctorDoc.exists() ? doctorDoc.data().consultationStartTime || '09:00 AM' : '09:00 AM';

  // Fetch all appointments for doctor on date
  const apptsSnapshot = await getDocs(
    query(
      collection(db, 'appointments'), 
      where('doctorId', '==', doctorId), 
      where('date', '==', date)
    )
  );

  const appointments = apptsSnapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      appointmentId: docSnap.id,
      ...data,
      time: data.time || '09:00 AM',
      status: data.status || 'UPCOMING'
    };
  }).filter(appt => appt.status !== 'CANCELLED');

  // Sort appointments
  appointments.sort((a, b) => {
    const timeDiff = timeToMinutes(a.time) - timeToMinutes(b.time);
    if (timeDiff !== 0) return timeDiff;
    const aCreated = a.createdAt?.seconds || 0;
    const bCreated = b.createdAt?.seconds || 0;
    return aCreated - bCreated;
  });

  // Calculate average consultation duration
  const completedAppts = appointments.filter(a => a.status === 'COMPLETED' && a.consultationStartedAt && a.consultationCompletedAt);
  let slotDuration = defaultSlotDuration;
  if (completedAppts.length > 0) {
    let totalDuration = 0;
    completedAppts.forEach(a => {
      try {
        const start = new Date(a.consultationStartedAt).getTime();
        const end = new Date(a.consultationCompletedAt).getTime();
        totalDuration += Math.floor((end - start) / 60000);
      } catch (e) {}
    });
    slotDuration = Math.max(5, Math.round(totalDuration / completedAppts.length));
  }

  const now = new Date();
  const nowMin = now.getHours() * 60 + now.getMinutes();

  let timelineMin = timeToMinutes(consultationStartTimeStr);

  let targetEstimatedWait = 0;
  let targetEstimatedDelay = 0;
  let targetPatientsAhead = 0;

  // Determine current serving token
  let currentServingToken = "0";
  const inProgressAppt = appointments.find(a => a.status === 'IN_PROGRESS');
  if (inProgressAppt) {
    currentServingToken = inProgressAppt.tokenNumber || "0";
  } else {
    const completedList = appointments.filter(a => a.status === 'COMPLETED');
    if (completedList.length > 0) {
      currentServingToken = completedList[completedList.length - 1].tokenNumber || "0";
    }
  }

  for (let i = 0; i < appointments.length; i++) {
    const appt = appointments[i];
    const scheduledStartMin = timeToMinutes(appt.time);

    let expectedStart = scheduledStartMin;
    if (appt.status === 'COMPLETED') {
      const startMin = scheduledStartMin;
      const endMin = startMin + slotDuration;
      expectedStart = startMin;
      timelineMin = endMin;
    } else if (appt.status === 'IN_PROGRESS') {
      const startMin = nowMin;
      const expectedEndMin = startMin + slotDuration;
      expectedStart = startMin;
      timelineMin = Math.max(expectedEndMin, nowMin);
    } else {
      expectedStart = Math.max(timelineMin, scheduledStartMin);
      timelineMin = expectedStart + slotDuration;
    }

    const estimatedWait = Math.max(0, expectedStart - nowMin);
    const estimatedDelay = Math.max(0, expectedStart - scheduledStartMin);

    if (appt.appointmentId === activeQueueItem.appointmentId) {
      targetEstimatedWait = estimatedWait;
      targetEstimatedDelay = estimatedDelay;
      
      // Count waiting patients ahead
      for (let j = 0; j < i; j++) {
        if (appointments[j].status === 'WAITING' || appointments[j].status === 'UPCOMING') {
          targetPatientsAhead++;
        }
      }
      break;
    }
  }

  let crowdLevel: 'LOW' | 'MEDIUM' | 'HIGH' = 'LOW';
  if (targetPatientsAhead > 10) crowdLevel = 'HIGH';
  else if (targetPatientsAhead > 4) crowdLevel = 'MEDIUM';

  let statusText = activeQueueItem.status;
  if (targetEstimatedDelay >= 20 && (statusText === 'WAITING' || statusText === 'UPCOMING')) {
    statusText = 'DOCTOR_RUNNING_LATE';
  }

  return {
    isActive: activeQueueItem.isActive !== undefined ? activeQueueItem.isActive : true,
    queueNumber: activeQueueItem.tokenNumber || '0',
    currentServingToken,
    status: statusText,
    patientsAhead: targetPatientsAhead,
    estimatedWaitMinutes: targetEstimatedWait,
    crowdLevel,
    department: activeQueueItem.department || ''
  };
};

export const getActivities = async (): Promise<Activity[]> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) return [];

  const snapshot = await getDocs(
    query(collection(db, 'activities'), where('userId', '==', currentUid))
  );

  const list = snapshot.docs.map(docSnap => {
    const data = docSnap.data();
    return {
      id: docSnap.id,
      userId: data.userId || '',
      type: data.type || 'GENERAL',
      title: data.title || '',
      description: data.description || '',
      timestamp: data.timestamp || ''
    };
  });

  return list.sort((a, b) => b.timestamp.localeCompare(a.timestamp)).slice(0, 10);
};

export const logActivity = async (activity: {
  type: string;
  title: string;
  description: string;
}): Promise<Activity> => {
  const currentUid = auth.currentUser?.uid;
  if (!currentUid) throw new Error('User not authenticated');

  const timestampStr = new Date().toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    hour12: true
  });

  const activityData = {
    userId: currentUid,
    type: activity.type,
    title: activity.title,
    description: activity.description,
    timestamp: timestampStr
  };

  const docRef = await addDoc(collection(db, 'activities'), activityData);
  return {
    id: docRef.id,
    ...activityData
  };
};

import { Notification, Activity, UserRole } from '../types';
import { getDoctorProfileByUid } from './doctors';

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

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

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
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return [];

  const res = await fetch(`${API_BASE_URL}/notifications/${currentUid}`);
  if (!res.ok) throw new Error('Failed to fetch notifications.');
  const list = await res.json();

  return list.map((data: any) => ({
    id: data.id,
    userId: data.userId || '',
    title: data.title || '',
    message: data.message || '',
    type: data.type || 'GENERAL',
    isRead: data.read === 1,
    timestamp: data.createdAt ? new Date(data.createdAt).toLocaleString() : ''
  }));
};

export const getUnreadNotificationsCount = async (): Promise<{ count: number }> => {
  const list = await getNotifications();
  const unread = list.filter(n => !n.isRead);
  return { count: unread.length };
};

export const markNotificationAsRead = async (id: string): Promise<Notification> => {
  const token = localStorage.getItem('medplus_token');
  const res = await fetch(`${API_BASE_URL}/notifications/${id}/read`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (!res.ok) throw new Error('Failed to mark notification as read');

  // Fetch updated notification or get it from list
  const list = await getNotifications();
  const updated = list.find(n => n.id === id);
  if (!updated) throw new Error('Notification not found');
  return updated;
};

export const markAllNotificationsAsRead = async (): Promise<{ msg: string }> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return { msg: 'User not authenticated' };

  const list = await getNotifications();
  const unread = list.filter(n => !n.isRead);
  
  const token = localStorage.getItem('medplus_token');
  // Loop and mark as read locally
  for (const n of unread) {
    await fetch(`${API_BASE_URL}/notifications/${n.id}/read`, {
      method: 'PUT',
      headers: { 'Authorization': `Bearer ${token}` }
    });
  }

  return { msg: 'All notifications marked as read' };
};

export const deleteNotification = async (id: string): Promise<{ msg: string }> => {
  const token = localStorage.getItem('medplus_token');
  await fetch(`${API_BASE_URL}/notifications/${id}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
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
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return null;

  const res = await fetch(`${API_BASE_URL}/appointments/patient/${currentUid}`);
  if (!res.ok) return null;
  const list = await res.json();

  const appointments = list.map((data: any) => ({
    appointmentId: data.id,
    doctorName: data.doctorName || '',
    department: data.department || '',
    status: data.status || 'PENDING',
    date: data.date || '',
    time: data.time || ''
  })).filter((appt: any) => appt.status === 'PENDING' || appt.status === 'ACTIVE' || appt.status === 'UPCOMING' || appt.status === 'CONFIRMED');

  if (appointments.length === 0) return null;

  // Sort chronologically
  appointments.sort((a: any, b: any) => {
    return (a.date + ' ' + a.time).localeCompare(b.date + ' ' + b.time);
  });

  return appointments[0];
};

export const getLiveQueueTracking = async (appointmentId?: string): Promise<LiveQueueData | null> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) return null;

  let activeQueueItem: any = null;

  // 1. Fetch queue items
  if (appointmentId) {
    const res = await fetch(`${API_BASE_URL}/queue?doctorId=${currentUid}`); // Fallback search
    if (res.ok) {
      const list = await res.json();
      activeQueueItem = list.find((item: any) => item.appointmentId === appointmentId && item.isActive);
    }
  } else {
    // Find next upcoming appointment
    const upcomingAppt = await getUpcomingAppointment();
    if (upcomingAppt) {
      const res = await fetch(`${API_BASE_URL}/queue?doctorId=${upcomingAppt.doctorName}`); // search via doctor
      // Or search queue records for this patient
      const patientQueueRes = await fetch(`${API_BASE_URL}/queue?doctorId=${currentUid}`); // Placeholder, we can query by doctor
    }
  }

  // To make it extremely reliable and independent of complex query nesting,
  // we can fetch the active queue by querying the doctor queue directly:
  // Let's query appointments for this patient
  const apptsRes = await fetch(`${API_BASE_URL}/appointments/patient/${currentUid}`);
  if (!apptsRes.ok) return null;
  const appts = await apptsRes.json();
  const activeAppt = appointmentId 
    ? appts.find((a: any) => a.id === appointmentId)
    : appts.find((a: any) => a.status === 'PENDING' || a.status === 'ACTIVE' || a.status === 'UPCOMING' || a.status === 'CONFIRMED');

  if (!activeAppt) return null;

  // Get the doctor queue list
  const doctorId = activeAppt.doctorId;
  const date = activeAppt.date;

  const queueRes = await fetch(`${API_BASE_URL}/queue?doctorId=${doctorId}&date=${encodeURIComponent(date)}`);
  if (!queueRes.ok) return null;
  const doctorQueue = await queueRes.json();

  activeQueueItem = doctorQueue.find((item: any) => item.appointmentId === activeAppt.id);
  if (!activeQueueItem) return null;

  // Fetch doctor profile to get slotDuration & consultationStartTime
  const doctorProfile = await getDoctorProfileByUid(doctorId);
  const defaultSlotDuration = doctorProfile.slotDuration || 15;
  const consultationStartTimeStr = doctorProfile.consultationStartTime || '09:00 AM';

  // Sort and filter active appointments
  const activeAppointments = appts.filter((a: any) => a.doctorId === doctorId && a.date === date && a.status !== 'CANCELLED');
  activeAppointments.sort((a: any, b: any) => {
    return timeToMinutes(a.time) - timeToMinutes(b.time);
  });

  let timelineMin = timeToMinutes(consultationStartTimeStr);
  const now = new Date();
  const nowMin = now.getHours() * 60 + now.getMinutes();

  let targetEstimatedWait = 0;
  let targetEstimatedDelay = 0;
  let targetPatientsAhead = 0;

  // Determine current serving token
  let currentServingToken = "0";
  const inProgressAppt = activeAppointments.find((a: any) => a.status === 'ACTIVE');
  if (inProgressAppt) {
    currentServingToken = inProgressAppt.tokenNumber || "0";
  } else {
    const completedList = activeAppointments.filter((a: any) => a.status === 'COMPLETED');
    if (completedList.length > 0) {
      currentServingToken = completedList[completedList.length - 1].tokenNumber || "0";
    }
  }

  for (let i = 0; i < activeAppointments.length; i++) {
    const appt = activeAppointments[i];
    const scheduledStartMin = timeToMinutes(appt.time);

    let expectedStart = scheduledStartMin;
    if (appt.status === 'COMPLETED') {
      const startMin = scheduledStartMin;
      const endMin = startMin + defaultSlotDuration;
      expectedStart = startMin;
      timelineMin = endMin;
    } else if (appt.status === 'ACTIVE') {
      const startMin = nowMin;
      const expectedEndMin = startMin + defaultSlotDuration;
      expectedStart = startMin;
      timelineMin = Math.max(expectedEndMin, nowMin);
    } else {
      expectedStart = Math.max(timelineMin, scheduledStartMin);
      timelineMin = expectedStart + defaultSlotDuration;
    }

    const estimatedWait = Math.max(0, expectedStart - nowMin);
    const estimatedDelay = Math.max(0, expectedStart - scheduledStartMin);

    if (appt.id === activeQueueItem.appointmentId) {
      targetEstimatedWait = estimatedWait;
      targetEstimatedDelay = estimatedDelay;
      
      // Count waiting patients ahead
      for (let j = 0; j < i; j++) {
        if (activeAppointments[j].status === 'PENDING') {
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
    isActive: activeQueueItem.isActive,
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
  const stored = localStorage.getItem('medplus_activities');
  if (stored) {
    try {
      return JSON.parse(stored);
    } catch (e) {
      return [];
    }
  }
  return [];
};

export const logActivity = async (activity: {
  type: string;
  title: string;
  description: string;
}): Promise<Activity> => {
  const currentUid = localStorage.getItem('medplus_uid');
  if (!currentUid) throw new Error('User not authenticated');

  const timestampStr = new Date().toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    hour12: true
  });

  const newActivity: Activity = {
    id: Math.random().toString(36).substring(2),
    userId: currentUid,
    type: activity.type,
    title: activity.title,
    description: activity.description,
    timestamp: timestampStr
  };

  const list = await getActivities();
  const updatedList = [newActivity, ...list].slice(0, 10);
  localStorage.setItem('medplus_activities', JSON.stringify(updatedList));

  return newActivity;
};

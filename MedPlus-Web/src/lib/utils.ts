// Simple zero-dependency class name joiner
export function cn(...inputs: any[]) {
  return inputs.filter(Boolean).map(x => String(x).trim()).filter(Boolean).join(' ');
}

/**
 * Format a Date to match the backend date format: "Aug 17, 2026"
 */
export const formatDateToBackend = (date: Date): string => {
  const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
  return date.toLocaleDateString('en-US', options);
};

/**
 * Format a date string to a human-readable date.
 */
export const formatHumanDate = (dateStr: string): string => {
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
  } catch (e) {
    return dateStr;
  }
};

/**
 * Get weekday name from date (e.g. "Monday")
 */
export const getWeekdayName = (dateStr: string): string => {
  try {
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleDateString('en-US', { weekday: 'long' });
  } catch (e) {
    return '';
  }
};

/**
 * Convert time string "HH:MM AM/PM" or "HH:MM" to minutes from midnight
 */
export const timeToMinutes = (timeStr: string): number => {
  if (!timeStr) return 0;
  const cleanTime = timeStr.trim();
  const timeParts = cleanTime.split(' ');
  const hm = timeParts[0].split(':');
  let hour = parseInt(hm[0]);
  const minute = parseInt(hm[1]);
  
  if (timeParts.length > 1) {
    const ampm = timeParts[1].toUpperCase();
    if (ampm === 'PM' && hour < 12) hour += 12;
    if (ampm === 'AM' && hour === 12) hour = 0;
  }
  return hour * 60 + minute;
};

/**
 * Convert minutes from midnight back to time string "HH:MM AM/PM"
 */
export const minutesToTimeStr = (min: number): string => {
  let hours = Math.floor(min / 60);
  const minutes = min % 60;
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  if (hours === 0) hours = 12;
  const minStr = minutes < 10 ? '0' + minutes : minutes;
  return `${hours}:${minStr} ${ampm}`;
};

/**
 * Generate slot array based on doctor settings
 */
export const generateSlots = (
  startTime: string,
  endTime: string,
  lunchStart?: string,
  lunchEnd?: string,
  breakStart?: string,
  breakEnd?: string,
  slotDuration: number = 60
): string[] => {
  const slots: string[] = [];
  if (!startTime || !endTime) return slots;

  const startMin = timeToMinutes(startTime);
  const endMin = timeToMinutes(endTime);
  const lunchStartMin = lunchStart ? timeToMinutes(lunchStart) : 0;
  const lunchEndMin = lunchEnd ? timeToMinutes(lunchEnd) : 0;
  const breakStartMin = breakStart ? timeToMinutes(breakStart) : 0;
  const breakEndMin = breakEnd ? timeToMinutes(breakEnd) : 0;

  let current = startMin;
  while (current + slotDuration <= endMin) {
    let inLunch = false;
    if (lunchStartMin > 0 && lunchEndMin > 0) {
      inLunch = current >= lunchStartMin && current < lunchEndMin;
    }

    let inBreak = false;
    if (breakStartMin > 0 && breakEndMin > 0) {
      inBreak = current >= breakStartMin && current < breakEndMin;
    }

    if (!inLunch && !inBreak) {
      slots.push(minutesToTimeStr(current));
    }
    current += slotDuration;
  }

  return slots;
};

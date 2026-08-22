import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getVerifiedDoctors } from '../../api/doctors';
import { getAppointmentsByDoctorId, bookAppointment, getAppointmentDetails, rescheduleAppointment } from '../../api/appointments';
import { Card, Button, Skeleton, StatusBadge } from '../../components/UI';
import { formatDateToBackend, getWeekdayName, timeToMinutes, minutesToTimeStr } from '../../lib/utils';
import { 
  ArrowLeft, 
  Calendar, 
  Clock, 
  DollarSign, 
  Stethoscope, 
  User, 
  CheckCircle,
  AlertCircle,
  ChevronRight,
  ShieldCheck,
  MapPin,
  Award,
  Video
} from 'lucide-react';
import { DoctorProfile, Appointment } from '../../types';

const DEPARTMENTS = [
  'General Medicine',
  'Cardiology',
  'Dermatology',
  'Pediatrics',
  'Neurology',
  'Orthopedics',
  'Gynecology'
];

interface Slot {
  time: string;
  available: boolean;
  bookedCount: number;
  maxCapacity: number;
}

const BookAppointment: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [searchParams] = useSearchParams();
  const rescheduleId = searchParams.get('rescheduleId');

  // Step state: 1: dept, 2: doctor, 3: date/time & slot, 4: confirm, 5: success
  const [step, setStep] = useState<1 | 2 | 3 | 4 | 5>(1);
  const [selectedDept, setSelectedDept] = useState<string | null>(null);
  const [selectedDoctor, setSelectedDoctor] = useState<DoctorProfile | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>(''); // YYYY-MM-DD
  const [selectedTime, setSelectedTime] = useState<string>(''); // HH:MM AM/PM
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const [receipt, setReceipt] = useState<Appointment | null>(null);

  const localToday = new Date();
  const todayStr = `${localToday.getFullYear()}-${String(localToday.getMonth() + 1).padStart(2, '0')}-${String(localToday.getDate()).padStart(2, '0')}`;

  // Fetch verified doctors list
  const { data: verifiedDoctors = [], isLoading: isDoctorsLoading } = useQuery({
    queryKey: ['verifiedDoctors'],
    queryFn: getVerifiedDoctors,
    enabled: step >= 1,
  });

  // Fetch appointment to reschedule if rescheduleId is provided
  const { data: rescheduleAppointmentDetails, isLoading: isRescheduleLoading } = useQuery({
    queryKey: ['rescheduleAppointment', rescheduleId],
    queryFn: () => rescheduleId ? getAppointmentDetails(rescheduleId) : null,
    enabled: !!rescheduleId,
  });

  // Pre-fill state when reschedule details are loaded
  useEffect(() => {
    if (rescheduleAppointmentDetails && verifiedDoctors.length > 0) {
      const docProfile = verifiedDoctors.find(d => d.uid === rescheduleAppointmentDetails.doctorId);
      if (docProfile) {
        setSelectedDept(rescheduleAppointmentDetails.department);
        setSelectedDoctor(docProfile);
        setStep(3); // Go straight to slot selection
      }
    }
  }, [rescheduleAppointmentDetails, verifiedDoctors]);

  // Fetch doctor's existing appointments on selected date to calculate slot loads
  const { data: doctorAppointments = [], isLoading: isSlotsLoading } = useQuery({
    queryKey: ['doctorAppointments', selectedDoctor?.uid, selectedDate],
    queryFn: () => getAppointmentsByDoctorId(selectedDoctor!.uid),
    enabled: !!selectedDoctor && !!selectedDate,
  });

  // Booking mutation
  const bookMutation = useMutation({
    mutationFn: bookAppointment,
    onSuccess: (data) => {
      setReceipt(data);
      queryClient.invalidateQueries({ queryKey: ['upcomingAppointment'] });
      queryClient.invalidateQueries({ queryKey: ['liveQueueStatus'] });
      queryClient.invalidateQueries({ queryKey: ['patientAppointments'] });
      setStep(5);
    },
    onError: (err: any) => {
      setToast({
        message: err.response?.data?.msg || err.message || 'Failed to book appointment slot.',
        type: 'error'
      });
      setTimeout(() => setToast(null), 4000);
    }
  });

  // Reschedule mutation
  const rescheduleMutation = useMutation({
    mutationFn: rescheduleAppointment,
    onSuccess: (data) => {
      setReceipt(data);
      queryClient.invalidateQueries({ queryKey: ['upcomingAppointment'] });
      queryClient.invalidateQueries({ queryKey: ['liveQueueStatus'] });
      queryClient.invalidateQueries({ queryKey: ['patientAppointments'] });
      setStep(5);
    },
    onError: (err: any) => {
      setToast({
        message: err.response?.data?.msg || err.message || 'Failed to reschedule appointment slot.',
        type: 'error'
      });
      setTimeout(() => setToast(null), 4000);
    }
  });

  // Filter doctors by selected department
  const filteredDoctors = verifiedDoctors.filter(doc => 
    selectedDept && 
    (doc.department?.toLowerCase() === selectedDept.toLowerCase() || 
     doc.specialization?.toLowerCase() === selectedDept.toLowerCase())
  );

  // Convert HTML date (YYYY-MM-DD) to backend format (Aug 17, 2026)
  const getBackendFormattedDate = () => {
    if (!selectedDate) return '';
    const [year, month, day] = selectedDate.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    return formatDateToBackend(date);
  };

  const getFilteredAppointmentsForDate = () => {
    const targetDateStr = getBackendFormattedDate();
    return doctorAppointments.filter(appt => appt.date === targetDateStr && appt.status !== 'CANCELLED');
  };

  // Generate bookable slots dynamically matching doctor's settings
  const getBookableSlots = (): Slot[] => {
    if (!selectedDoctor) return [];
    
    // Check if the doctor works on the selected day of the week
    if (selectedDate) {
      const selectedDayOfWeek = getWeekdayName(selectedDate);
      const isWorkingDay = selectedDoctor.workingDays?.some(wDay => 
        wDay.toLowerCase().substring(0, 3) === selectedDayOfWeek.toLowerCase().substring(0, 3)
      );
      if (!isWorkingDay) return [];
    }

    const slots: Slot[] = [];
    const startTimeMin = timeToMinutes(selectedDoctor.consultationStartTime || '09:00 AM');
    const endTimeMin = timeToMinutes(selectedDoctor.consultationEndTime || '05:00 PM');
    const lunchStartMin = selectedDoctor.lunchStartTime ? timeToMinutes(selectedDoctor.lunchStartTime) : 0;
    const lunchEndMin = selectedDoctor.lunchEndTime ? timeToMinutes(selectedDoctor.lunchEndTime) : 0;
    const breakStartMin = selectedDoctor.breakStartTime ? timeToMinutes(selectedDoctor.breakStartTime) : 0;
    const breakEndMin = selectedDoctor.breakEndTime ? timeToMinutes(selectedDoctor.breakEndTime) : 0;
    
    const slotDuration = selectedDoctor.slotDuration || 15;
    const maxCapacity = Math.floor(60 / slotDuration); // bookings per hour

    const activeAppts = getFilteredAppointmentsForDate();

    // Generate slot times (hourly blocks)
    for (let current = startTimeMin; current < endTimeMin; current += 60) {
      // Skip slots that have already passed for today
      if (selectedDate === todayStr) {
        const now = new Date();
        const currentMinFromMidnight = now.getHours() * 60 + now.getMinutes();
        if (current <= currentMinFromMidnight) {
          continue;
        }
      }

      const currentEnd = current + 60;

      // Check lunch window
      if (lunchStartMin > 0 && lunchEndMin > 0) {
        if (current < lunchEndMin && currentEnd > lunchStartMin) continue;
      }

      // Check doctor breaks
      if (breakStartMin > 0 && breakEndMin > 0) {
        if (current < breakEndMin && currentEnd > breakStartMin) continue;
      }

      const slotTimeText = minutesToTimeStr(current);
      const bookedCount = activeAppts.filter(appt => appt.time === slotTimeText).length;

      slots.push({
        time: slotTimeText,
        available: bookedCount < maxCapacity,
        bookedCount,
        maxCapacity
      });
    }

    return slots;
  };

  const handleConfirmBooking = () => {
    if (!selectedDoctor || !selectedDate || !selectedTime) return;
    
    if (rescheduleId) {
      rescheduleMutation.mutate({
        appointmentId: rescheduleId,
        doctorId: selectedDoctor.uid,
        doctorName: selectedDoctor.fullName,
        department: selectedDoctor.specialization || selectedDoctor.department || 'General Medicine',
        date: getBackendFormattedDate(),
        time: selectedTime,
        reason: 'Rescheduled via portal'
      });
    } else {
      bookMutation.mutate({
        doctorId: selectedDoctor.uid,
        doctorName: selectedDoctor.fullName,
        department: selectedDoctor.specialization || selectedDoctor.department || 'General Medicine',
        date: getBackendFormattedDate(),
        time: selectedTime,
        reason: 'Standard Consultation check'
      });
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      
      {/* Toast Alert */}
      {toast && (
        <div className="fixed top-5 left-1/2 -translate-x-1/2 z-55 flex items-center gap-2.5 px-5 py-3.5 border border-rose-500 rounded-2xl shadow-xl text-sm font-semibold bg-rose-600 text-white max-w-md animate-in fade-in">
          <AlertCircle size={18} />
          <span>{toast.message}</span>
        </div>
      )}

      {/* Step navigation bar */}
      <div className="flex items-center gap-4 mb-6 select-none bg-white p-4 rounded-2xl border border-slate-100/70 shadow-xs">
        {step > 1 && step < 5 && (
          <button 
            onClick={() => setStep((prev) => (prev - 1) as any)} 
            className="p-2 border border-slate-200 rounded-xl hover:bg-slate-50 hover:border-slate-300 text-slate-500 hover:text-slate-900 bg-white transition-all cursor-pointer active:scale-95"
          >
            <ArrowLeft size={16} />
          </button>
        )}
        <div className="flex flex-col">
          <h3 className="text-base font-extrabold text-slate-900 tracking-tight leading-snug">
            {step === 1 && 'Select a Specialty Department'}
            {step === 2 && `Available Specialists (${selectedDept})`}
            {step === 3 && (rescheduleId ? 'Reschedule Appointment Slot' : 'Choose Date & Appointment Block')}
            {step === 4 && (rescheduleId ? 'Confirm Rescheduled Details' : 'Confirm Appointment Summary')}
            {step === 5 && (rescheduleId ? 'Appointment Rescheduled!' : 'Booking Confirmed!')}
          </h3>
          {step < 5 && (
            <span className="text-[9px] font-bold text-slate-400 uppercase tracking-widest mt-0.5">
              Step {step} of 4 &bull; Progress Form
            </span>
          )}
        </div>
      </div>

      {/* STEP 1: Department Grid */}
      {step === 1 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 select-none">
          {DEPARTMENTS.map(dept => (
            <div 
              key={dept} 
              onClick={() => { setSelectedDept(dept); setStep(2); }}
              className="bg-white border border-slate-100 p-6 rounded-2xl text-center cursor-pointer hover:border-medical-blue-500 hover:shadow-[0_8px_30px_rgba(0,0,0,0.03)] hover:-translate-y-0.5 transition-all duration-300 flex flex-col items-center gap-4 group shadow-xs"
            >
              <div className="w-12 h-12 rounded-2xl bg-medical-blue-50 border border-medical-blue-100/30 text-medical-blue-600 flex items-center justify-center group-hover:scale-105 group-hover:bg-medical-blue-600 group-hover:text-white transition-all duration-300 shadow-sm shadow-medical-blue-500/5">
                <Stethoscope size={24} />
              </div>
              <span className="font-extrabold text-sm text-slate-800 group-hover:text-medical-blue-600 transition-colors">{dept}</span>
            </div>
          ))}
        </div>
      )}

      {/* STEP 2: Doctors List */}
      {step === 2 && (
        <div>
          {isDoctorsLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Skeleton height="190px" className="rounded-3xl" />
              <Skeleton height="190px" className="rounded-3xl" />
            </div>
          ) : filteredDoctors.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {filteredDoctors.map(doc => (
                <Card key={doc.uid} className="flex flex-col justify-between p-6 rounded-3xl" hoverEffect={true}>
                  <div>
                    <div className="flex items-start gap-4 mb-4">
                      <div className="w-12 h-12 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-bold text-sm uppercase flex-shrink-0">
                        {doc.fullName.split(' ').map((n: string) => n[0]).join('').substring(0, 2).toUpperCase()}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5 flex-wrap">
                          <h4 className="font-extrabold text-sm text-slate-800 truncate">Dr. {doc.fullName}</h4>
                          <ShieldCheck size={15} className="text-emerald-500 flex-shrink-0" />
                        </div>
                        <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mt-0.5 flex items-center gap-1 flex-wrap">
                          <Award size={12} className="text-slate-400" />
                          <span>{doc.qualification || 'MBBS'}</span>
                          <span>&bull;</span>
                          <span>{doc.specialization}</span>
                        </p>
                      </div>
                    </div>

                    <div className="space-y-2 border-t border-slate-100 pt-4 text-xs font-semibold text-slate-500">
                      <div className="flex items-center justify-between">
                        <span className="flex items-center gap-1.5"><Clock size={13} className="text-slate-400" /> Consultation Hours</span>
                        <span className="text-slate-800 font-bold">{doc.consultationStartTime} - {doc.consultationEndTime}</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="flex items-center gap-1.5"><DollarSign size={13} className="text-slate-400" /> Consultation Fee</span>
                        <span className="text-medical-teal-650 font-extrabold text-sm">${doc.consultationFee || 15}</span>
                      </div>
                    </div>
                  </div>

                  <Button 
                    onClick={() => { setSelectedDoctor(doc); setStep(3); }} 
                    className="w-full mt-6 py-2.5 rounded-xl font-bold shadow-md shadow-medical-blue-500/5 hover:-translate-y-0.5 active:translate-y-0"
                  >
                    Select Doctor
                  </Button>
                </Card>
              ))}
            </div>
          ) : (
            <div className="bg-white border border-slate-100 rounded-3xl p-8 text-center max-w-md mx-auto shadow-xs">
              <p className="text-sm text-slate-500 font-semibold mb-5">No verified specialists are available in this department at the moment.</p>
              <Button onClick={() => setStep(1)} variant="outline" className="text-xs py-2 px-5 rounded-xl shadow-xs">
                Go Back
              </Button>
            </div>
          )}
        </div>
      )}

      {/* STEP 3: Date & Slot Selector */}
      {step === 3 && selectedDoctor && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <Card className="h-full flex flex-col justify-between p-6 rounded-3xl" hoverEffect={false}>
              <div className="space-y-4">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">1. Pick Appointment Date</label>
                <input 
                  type="date" 
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200" 
                  min={todayStr}
                  value={selectedDate}
                  onChange={(e) => { setSelectedDate(e.target.value); setSelectedTime(''); }}
                  required
                />

                <div className="border-t border-slate-100 pt-5 space-y-3.5 text-slate-600 select-none">
                  <div className="flex items-center gap-3">
                    <User size={16} className="text-slate-400" />
                    <span className="text-xs font-extrabold text-slate-800">Dr. {selectedDoctor.fullName}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <DollarSign size={16} className="text-slate-400" />
                    <span className="text-xs font-extrabold text-slate-800">Consultation Fee: ${selectedDoctor.consultationFee || 15}</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <Calendar size={16} className="text-slate-400" />
                    <span className="text-xs font-semibold text-slate-500 leading-relaxed">
                      Workdays: {selectedDoctor.workingDays?.join(', ')}
                    </span>
                  </div>
                </div>
              </div>
            </Card>
          </div>

          <div>
            <Card className="h-full flex flex-col justify-between p-6 rounded-3xl" hoverEffect={false}>
              <div>
                <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider mb-4 select-none">2. Select Available Slot</h4>
                {!selectedDate ? (
                  <div className="text-center py-12 text-xs font-semibold text-slate-400 select-none">
                    Select a date on the calendar to view slots.
                  </div>
                ) : isSlotsLoading ? (
                  <div className="grid grid-cols-2 gap-2">
                    <Skeleton height="55px" className="rounded-xl" />
                    <Skeleton height="55px" className="rounded-xl" />
                  </div>
                ) : getBookableSlots().length > 0 ? (
                  <div className="grid grid-cols-2 gap-2.5">
                    {getBookableSlots().map(slot => (
                      <button
                        key={slot.time}
                        disabled={!slot.available}
                        onClick={() => setSelectedTime(slot.time)}
                        className={`p-3 border rounded-xl text-center select-none transition-all flex flex-col items-center bg-transparent cursor-pointer active:scale-95 duration-150 ${
                          selectedTime === slot.time 
                            ? 'border-medical-blue-600 bg-medical-blue-50/50 text-medical-blue-600 font-extrabold' 
                            : slot.available 
                              ? 'border-slate-200 text-slate-800 hover:border-medical-blue-500' 
                              : 'border-dashed border-slate-200 text-slate-350 opacity-40 cursor-not-allowed'
                        }`}
                      >
                        <span className="text-xs font-bold">{slot.time}</span>
                        <span className={`text-[9px] font-bold mt-0.5 ${selectedTime === slot.time ? 'text-medical-blue-500' : 'text-slate-400'}`}>
                          {slot.available ? `${slot.maxCapacity - slot.bookedCount} slots left` : 'Fully Booked'}
                        </span>
                      </button>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12 text-xs font-semibold text-slate-400 leading-relaxed select-none">
                    Doctor is not available or has no schedule configured for this day. Please choose another date.
                  </div>
                )}
              </div>

              {selectedTime && (
                <Button 
                  onClick={() => setStep(4)} 
                  className="w-full mt-6 py-2.5 rounded-xl font-bold shadow-md shadow-medical-blue-500/5 hover:-translate-y-0.5 active:translate-y-0"
                >
                  Proceed to Confirmation
                </Button>
              )}
            </Card>
          </div>
        </div>
      )}

      {/* STEP 4: Confirmation Recap */}
      {step === 4 && selectedDoctor && (
        <div className="max-w-md mx-auto">
          <Card className="border-2 border-medical-blue-600/70 p-7 rounded-3xl" hoverEffect={false}>
            <div className="text-center border-b border-slate-100 pb-5 mb-5 select-none">
              <h4 className="text-[10px] text-slate-400 font-bold uppercase tracking-wider mb-1">APPOINTMENT RECAP</h4>
              <p className="font-black text-xl text-slate-900">Dr. {selectedDoctor.fullName}</p>
              <p className="text-xs font-bold text-slate-500 mt-0.5">{selectedDoctor.specialization || selectedDoctor.department}</p>
            </div>

            <div className="flex flex-col gap-3.5 mb-6 text-slate-650 font-bold text-xs select-none">
              <div className="flex items-center justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Date</span>
                <span className="text-slate-800 font-extrabold">{getBackendFormattedDate()}</span>
              </div>
              <div className="flex items-center justify-between border-b border-slate-100 pb-2.5">
                <span className="text-slate-400">Consultation Hour Block</span>
                <span className="text-slate-800 font-extrabold">{selectedTime}</span>
              </div>
              <div className="flex items-center justify-between pb-1">
                <span className="text-slate-400">Consultation Fee</span>
                <span className="text-medical-teal-650 font-black text-sm">${selectedDoctor.consultationFee || 15}</span>
              </div>
            </div>

            <div className="flex gap-3">
              <Button onClick={() => setStep(3)} variant="outline" className="flex-1 py-2.5 text-xs rounded-xl font-bold">
                Modify Slot
              </Button>
              <Button 
                onClick={handleConfirmBooking} 
                className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-md" 
                loading={bookMutation.isPending || rescheduleMutation.isPending}
              >
                Confirm Booking
              </Button>
            </div>
          </Card>
        </div>
      )}

      {/* STEP 5: Success Receipt */}
      {step === 5 && receipt && selectedDoctor && (
        <div className="max-w-md mx-auto text-center py-8">
          <div className="inline-flex p-4.5 bg-emerald-50 text-emerald-600 rounded-full mb-5 shadow-inner shadow-emerald-500/5 animate-bounce">
            <CheckCircle size={40} />
          </div>
          <h2 className="text-xl font-black text-emerald-600 mb-1">
            {rescheduleId ? 'Appointment Rescheduled!' : 'Booking Confirmed!'}
          </h2>
          <p className="text-xs text-slate-400 font-semibold mb-6">
            {rescheduleId 
              ? 'Your appointment has been rescheduled and synchronized with the doctor\'s queue.'
              : 'Your appointment is scheduled and synchronized with the doctor\'s queue.'}
          </p>

          <Card className="mb-6 text-left bg-slate-50/70 border-dashed border-slate-200 p-5 rounded-3xl" hoverEffect={false}>
            <div className="space-y-3.5 text-xs text-slate-655 font-bold">
              <div className="flex items-center justify-between border-b border-slate-200/60 pb-2.5">
                <span className="text-slate-400">APPOINTMENT ID</span>
                <span className="font-mono text-slate-800 text-[10px] select-all tracking-tight font-semibold">{receipt._id}</span>
              </div>
              <div className="flex items-center justify-between border-b border-slate-200/60 pb-2.5">
                <span className="text-slate-400">DOCTOR</span>
                <span className="text-slate-800">Dr. {selectedDoctor.fullName}</span>
              </div>
              <div className="flex items-center justify-between border-b border-slate-200/60 pb-2.5">
                <span className="text-slate-400">DATE & TIME</span>
                <span className="text-slate-800">{receipt.date} at {receipt.time}</span>
              </div>
              <div className="flex items-center justify-between pt-0.5">
                <span className="text-slate-400">ASSIGNED TOKEN</span>
                <span className="text-base font-black text-medical-blue-650">#{receipt.tokenNumber}</span>
              </div>
            </div>
          </Card>

          <div className="flex gap-3">
            <Button onClick={() => navigate('/patient/appointments')} variant="outline" className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-xs">
              My Appointments
            </Button>
            <Button onClick={() => navigate('/patient/dashboard')} className="flex-1 py-2.5 text-xs rounded-xl font-bold shadow-md">
              Go to Dashboard
            </Button>
          </div>
        </div>
      )}

    </div>
  );
};

export default BookAppointment;

const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const Appointment = require('../models/Appointment');
const QueueItem = require('../models/QueueItem');
const User = require('../models/User');
const DoctorProfile = require('../models/DoctorProfile');

const timeToMinutes = (timeStr) => {
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

const generateSlotsForDoctor = (doctorProfile) => {
  const slots = [];
  const startTime = doctorProfile.consultationStartTime;
  const endTime = doctorProfile.consultationEndTime;
  const lunchStart = doctorProfile.lunchStartTime;
  const lunchEnd = doctorProfile.lunchEndTime;
  const breakStart = doctorProfile.breakStartTime;
  const breakEnd = doctorProfile.breakEndTime;

  if (!startTime || !endTime) return slots;

  const startMin = timeToMinutes(startTime);
  const endMin = timeToMinutes(endTime);
  const lunchStartMin = timeToMinutes(lunchStart);
  const lunchEndMin = timeToMinutes(lunchEnd);
  const breakStartMin = timeToMinutes(breakStart);
  const breakEndMin = timeToMinutes(breakEnd);

  const duration = 60; // Enforce hourly slots in backend list matching the UI
  let current = startMin;

  const formatMinutesToTime = (min) => {
    let hours = Math.floor(min / 60);
    const minutes = min % 60;
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    if (hours === 0) hours = 12;
    const minStr = minutes < 10 ? '0' + minutes : minutes;
    return `${hours}:${minStr} ${ampm}`;
  };

  while (current + duration <= endMin) {
    let inLunch = false;
    if (lunchStartMin > 0 && lunchEndMin > 0) {
      inLunch = current >= lunchStartMin && current < lunchEndMin;
    }

    let inBreak = false;
    if (breakStartMin > 0 && breakEndMin > 0) {
      inBreak = current >= breakStartMin && current < breakEndMin;
    }

    if (!inLunch && !inBreak) {
      slots.push(formatMinutesToTime(current));
    }
    current += duration;
  }

  return slots;
};

const recalculateQueue = async (doctorId, date) => {
  console.log(`[QUEUE_DEBUG] Recalculating queue for Doctor: ${doctorId}, Date: ${date}`);
  try {
    const doctorProfile = await DoctorProfile.findOne({ uid: doctorId });
    const defaultSlotDuration = doctorProfile ? (doctorProfile.slotDuration || 15) : 15;

    // Find all active (non-cancelled) appointments for the doctor and date
    const appointments = await Appointment.find({
      doctorId,
      date,
      status: { $ne: 'CANCELLED' }
    });

    // Sort chronologically by appointment slot time, and then by booking creation date
    appointments.sort((a, b) => {
      const timeDiff = timeToMinutes(a.time) - timeToMinutes(b.time);
      if (timeDiff !== 0) return timeDiff;
      return new Date(a.createdAt || 0) - new Date(b.createdAt || 0);
    });

    // 1. Calculate the average observed consultation duration for completed appointments today
    const completedAppts = appointments.filter(a => a.status === 'COMPLETED' && a.consultationStartedAt && a.consultationCompletedAt);
    let slotDuration = defaultSlotDuration;
    if (completedAppts.length > 0) {
      let totalDuration = 0;
      completedAppts.forEach(a => {
        const diffMs = new Date(a.consultationCompletedAt) - new Date(a.consultationStartedAt);
        totalDuration += Math.floor(diffMs / 60000);
      });
      slotDuration = Math.max(5, Math.round(totalDuration / completedAppts.length));
      console.log(`[QUEUE_DEBUG] Average observed consultation duration: ${slotDuration} mins (based on ${completedAppts.length} completed)`);
    } else {
      console.log(`[QUEUE_DEBUG] Using default slot duration: ${slotDuration} mins`);
    }

    const now = new Date();
    // Get current minutes from midnight
    const nowMin = now.getHours() * 60 + now.getMinutes();

    // 2. Timeline simulation
    let timelineMin = doctorProfile ? timeToMinutes(doctorProfile.consultationStartTime) : 540; // Default 9:00 AM

    for (let i = 0; i < appointments.length; i++) {
      const appt = appointments[i];
      const scheduledStartMin = timeToMinutes(appt.time);

      let expectedStart;
      if (appt.status === 'COMPLETED') {
        const startMin = appt.consultationStartedAt ? (new Date(appt.consultationStartedAt).getHours() * 60 + new Date(appt.consultationStartedAt).getMinutes()) : scheduledStartMin;
        const endMin = appt.consultationCompletedAt ? (new Date(appt.consultationCompletedAt).getHours() * 60 + new Date(appt.consultationCompletedAt).getMinutes()) : (startMin + slotDuration);
        
        expectedStart = startMin;
        timelineMin = endMin;
      } else if (appt.status === 'IN_PROGRESS') {
        const startMin = appt.consultationStartedAt ? (new Date(appt.consultationStartedAt).getHours() * 60 + new Date(appt.consultationStartedAt).getMinutes()) : nowMin;
        const expectedEndMin = startMin + slotDuration;
        
        expectedStart = startMin;
        timelineMin = Math.max(expectedEndMin, nowMin);
      } else {
        expectedStart = Math.max(timelineMin, scheduledStartMin);
        timelineMin = expectedStart + slotDuration;
      }

      const estimatedWait = Math.max(0, expectedStart - nowMin);
      const estimatedDelay = Math.max(0, expectedStart - scheduledStartMin);

      console.log(`[QUEUE_DEBUG] Appt #${appt.tokenNumber}: Scheduled=${appt.time} (${scheduledStartMin}m), ExpectedStart=${expectedStart}m, EstWait=${estimatedWait}m, EstDelay=${estimatedDelay}m`);

      // Update QueueItem precalculated estimated wait minutes
      const queueItem = await QueueItem.findOne({ appointmentId: appt._id.toString() });
      if (queueItem) {
        queueItem.estimatedWaitMinutes = estimatedWait;
        queueItem.consultationStartedAt = appt.consultationStartedAt;
        queueItem.consultationCompletedAt = appt.consultationCompletedAt;

        // Calculate patients waiting ahead of this item
        let waitingCountAhead = 0;
        for (let j = 0; j < i; j++) {
          if (appointments[j].status === 'WAITING' || appointments[j].status === 'UPCOMING') {
            waitingCountAhead++;
          }
        }
        
        const Notification = require('../models/Notification');

        // Trigger Queue Approaching and Next notifications
        if ((appt.status === 'WAITING' || appt.status === 'UPCOMING') && queueItem.isActive) {
          if (waitingCountAhead === 1 && queueItem.lastNotifiedPosition !== 2) {
            queueItem.lastNotifiedPosition = 2;
            const approachingNotif = new Notification({
              userId: appt.patientId,
              title: 'Appointment Approaching',
              message: `Your appointment with Dr. ${appt.doctorName} is approaching. You are #2 in today's queue.`,
              type: 'QUEUE'
            });
            await approachingNotif.save();
          } else if (waitingCountAhead === 0 && queueItem.lastNotifiedPosition !== 1) {
            queueItem.lastNotifiedPosition = 1;
            const nextNotif = new Notification({
              userId: appt.patientId,
              title: 'You Are Next',
              message: `You are next up for Dr. ${appt.doctorName}. Please be ready for your consultation.`,
              type: 'QUEUE'
            });
            await nextNotif.save();
          }
        }

        // Trigger Delay notification if delay increased by 10+ minutes
        if (estimatedDelay >= 10 && (appt.status === 'WAITING' || appt.status === 'UPCOMING') && queueItem.isActive) {
          const lastNotified = queueItem.lastNotifiedWaitMinutes || 0;
          if (estimatedWait >= lastNotified + 10) {
            queueItem.lastNotifiedWaitMinutes = estimatedWait;
            const delayNotif = new Notification({
              userId: appt.patientId,
              title: 'Doctor Running Late',
              message: `Dr. ${appt.doctorName} is running behind schedule. Your estimated waiting time is now approximately ${estimatedWait} minutes.`,
              type: 'QUEUE'
            });
            await delayNotif.save();
            console.log(`[QUEUE_DEBUG] Dispatched delay alert to patient ${appt.patientId} (wait: ${estimatedWait}m)`);
          }
        }

        await queueItem.save();
      }
    }
  } catch (err) {
    console.error(`[QUEUE_DEBUG] Error in recalculateQueue: ${err.message}`);
  }
};

// @route   POST api/appointments
// @desc    Book a new appointment
// @access  Private (Patient)
router.post('/', auth, async (req, res) => {
  const { doctorId, doctorName, department, date, time, reason } = req.body;

  try {
    const patientUser = await User.findById(req.user.id);
    if (!patientUser) {
      return res.status(404).json({ msg: 'Patient user not found' });
    }

    // 1. Check doctor profile details
    const doctorProfile = await DoctorProfile.findOne({ uid: doctorId });
    if (!doctorProfile) {
      return res.status(404).json({ msg: 'Doctor profile not found.' });
    }

    // 2. Check if doctor is verified/approved
    if (doctorProfile.verificationStatus !== 'VERIFIED' && doctorProfile.verificationStatus !== 'APPROVED') {
      return res.status(400).json({ msg: 'Doctor is not currently approved.' });
    }

    // 3. Check for conflicting appointments for the patient at the same date and time
    const patientConflict = await Appointment.findOne({ patientId: req.user.id, date, time, status: 'UPCOMING' });
    if (patientConflict) {
      return res.status(400).json({ msg: 'You already have an appointment booked at this time.' });
    }

    // 4. Validate doctor weekday availability
    const bookingDate = new Date(date);
    const daysOfWeek = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const selectedDayName = daysOfWeek[bookingDate.getDay()];
    
    const isWorkingDay = doctorProfile.workingDays.some(d => 
      d.toLowerCase() === selectedDayName.toLowerCase() || 
      d.toLowerCase().substring(0, 3) === selectedDayName.toLowerCase().substring(0, 3)
    );
    if (!isWorkingDay) {
      return res.status(400).json({ msg: `Doctor is not available on ${selectedDayName}s.` });
    }

    // 5. Validate working hours and lunch break
    const bookingTimeMinutes = timeToMinutes(time);
    const startMinutes = timeToMinutes(doctorProfile.consultationStartTime);
    const endMinutes = timeToMinutes(doctorProfile.consultationEndTime);
    const lunchStartMinutes = timeToMinutes(doctorProfile.lunchStartTime);
    const lunchEndMinutes = timeToMinutes(doctorProfile.lunchEndTime);
   
    const bookingEndTimeMinutes = bookingTimeMinutes + 60;
    const breakStartMinutes = timeToMinutes(doctorProfile.breakStartTime);
    const breakEndMinutes = timeToMinutes(doctorProfile.breakEndTime);
   
    if (bookingTimeMinutes < startMinutes || bookingEndTimeMinutes > endMinutes) {
      return res.status(400).json({ msg: "Selected time is outside the doctor's working hours." });
    }
   
    if (lunchStartMinutes > 0 && lunchEndMinutes > 0) {
      if (bookingTimeMinutes < lunchEndMinutes && bookingEndTimeMinutes > lunchStartMinutes) {
        return res.status(400).json({ msg: "Selected time overlaps with the doctor's lunch break." });
      }
    }

    if (breakStartMinutes > 0 && breakEndMinutes > 0) {
      if (bookingTimeMinutes < breakEndMinutes && bookingEndTimeMinutes > breakStartMinutes) {
        return res.status(400).json({ msg: "Selected time overlaps with the doctor's other break." });
      }
    }

    // Validate slot capacity (max bookings per hour)
    const bookedCount = await Appointment.countDocuments({ doctorId, date, time, status: 'UPCOMING' });
    const maxCapacity = Math.floor(60 / (doctorProfile.slotDuration || 15));
    if (bookedCount >= maxCapacity) {
      return res.status(400).json({ msg: 'This appointment slot is fully booked.' });
    }

    // Determine the slot index
    const slots = generateSlotsForDoctor(doctorProfile);
    let slotIdx = slots.indexOf(time.trim());
    if (slotIdx === -1) {
      slotIdx = slots.findIndex(s => timeToMinutes(s) === timeToMinutes(time));
    }
    
    // Count ALL appointments booked for this slot time (regardless of active status, ensuring tokens don't overlap)
    const slotBookingCount = await Appointment.countDocuments({ doctorId, date, time });
    
    let tokenNumber;
    if (slotIdx !== -1) {
      tokenNumber = ((slotIdx * maxCapacity) + 1 + slotBookingCount).toString();
    } else {
      const totalBookingsForDay = await Appointment.countDocuments({ doctorId, date });
      tokenNumber = (totalBookingsForDay + 1).toString();
    }

    // 2. Combine date & time into a Date object for the timestamp field safely
    let timestamp;
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

        const months = {
          Jan: 0, Feb: 1, Mar: 2, Apr: 3, May: 4, Jun: 5,
          Jul: 6, Aug: 7, Sep: 8, Oct: 9, Nov: 10, Dec: 11
        };
        const monthIndex = months[monthStr] !== undefined ? months[monthStr] : 0;
        timestamp = new Date(parseInt(yearStr), monthIndex, parseInt(dayStr), hours, minutes);
      } else {
        timestamp = new Date(`${date}T${time}`);
        if (isNaN(timestamp.getTime())) {
          timestamp = new Date(`${date} ${time}`);
        }
      }
      if (isNaN(timestamp.getTime())) {
        timestamp = new Date();
      }
    } catch (e) {
      timestamp = new Date();
    }

    const appointment = new Appointment({
      patientId: req.user.id,
      patientName: patientUser.fullName,
      doctorId,
      doctorName,
      department,
      date,
      time,
      status: 'UPCOMING',
      tokenNumber,
      timestamp
    });

    await appointment.save();

    // 3. Create a corresponding QueueItem entry
    const queueItem = new QueueItem({
      appointmentId: appointment._id.toString(),
      doctorId,
      patientId: req.user.id,
      patientName: patientUser.fullName,
      tokenNumber,
      status: 'WAITING',
      department,
      date,
      isActive: true,
      estimatedWaitMinutes: 0 // Will be recalculated below
    });
    await queueItem.save();

    // Recalculate queue chronologically for doctor & date
    await recalculateQueue(doctorId, date);

    // Retrieve final assigned token number
    const updatedAppt = await Appointment.findById(appointment._id);
    const finalTokenNumber = updatedAppt ? updatedAppt.tokenNumber : tokenNumber;

    // 4. Create standard patient notification
    const Notification = require('../models/Notification');
    const patientNotification = new Notification({
      userId: req.user.id,
      title: 'Appointment Booked',
      message: `Your appointment with Dr. ${doctorName} on ${date} at ${time} is confirmed. Token #${finalTokenNumber}.`,
      type: 'APPOINTMENT'
    });
    await patientNotification.save();

    // 5. Create standard doctor notification
    const doctorNotification = new Notification({
      userId: doctorId,
      title: 'New Appointment Booked',
      message: `${patientUser.fullName} has booked an appointment for ${date} at ${time}.`,
      type: 'APPOINTMENT'
    });
    await doctorNotification.save();

    res.json(updatedAppt || appointment);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/appointments/patient
// @desc    Get all appointments for the logged in patient
// @access  Private (Patient)
router.get('/patient', auth, async (req, res) => {
  try {
    const appointments = await Appointment.find({ patientId: req.user.id }).sort({ timestamp: -1 });
    res.json(appointments);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/appointments/doctor
// @desc    Get all appointments for the logged in doctor
// @access  Private (Doctor)
router.get('/doctor', auth, async (req, res) => {
  try {
    const appointments = await Appointment.find({ doctorId: req.user.id }).sort({ timestamp: -1 });
    res.json(appointments);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/appointments/doctor/:doctorId
// @desc    Get all appointments for a specific doctor
// @access  Private
router.get('/doctor/:doctorId', auth, async (req, res) => {
  try {
    const appointments = await Appointment.find({ doctorId: req.params.doctorId }).sort({ timestamp: -1 });
    res.json(appointments);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/appointments/:id
// @desc    Get appointment details by ID
// @access  Private
router.get('/:id', auth, async (req, res) => {
  try {
    const appointment = await Appointment.findById(req.params.id);
    if (!appointment) {
      return res.status(404).json({ msg: 'Appointment not found' });
    }
    // Verify that the logged in user is either the patient or the doctor
    if (appointment.patientId !== req.user.id && appointment.doctorId !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(401).json({ msg: 'Not authorized' });
    }
    res.json(appointment);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/appointments/:id/cancel
// @desc    Cancel an appointment
// @access  Private
router.post('/:id/cancel', auth, async (req, res) => {
  try {
    const appointment = await Appointment.findById(req.params.id);
    if (!appointment) {
      return res.status(404).json({ msg: 'Appointment not found' });
    }

    appointment.status = 'CANCELLED';
    await appointment.save();

    // Disable the QueueItem
    await QueueItem.findOneAndUpdate(
      { appointmentId: appointment._id.toString() },
      { $set: { status: 'CANCELLED', isActive: false } }
    );

    // Recalculate queue/token order chronologically for this doctor and date
    await recalculateQueue(appointment.doctorId, appointment.date);

    // Create notifications
    const Notification = require('../models/Notification');
    const patientNotification = new Notification({
      userId: appointment.patientId,
      title: 'Appointment Cancelled',
      message: `Your appointment with Dr. ${appointment.doctorName} on ${appointment.date} was cancelled.`,
      type: 'APPOINTMENT'
    });
    await patientNotification.save();

    const doctorNotification = new Notification({
      userId: appointment.doctorId,
      title: 'Appointment Cancelled',
      message: `Appointment for ${appointment.patientName} on ${appointment.date} has been cancelled.`,
      type: 'APPOINTMENT'
    });
    await doctorNotification.save();

    res.json(appointment);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   PUT api/appointments/:id/status
// @desc    Update appointment status
// @access  Private (Doctor or Admin)
router.put('/:id/status', auth, async (req, res) => {
  const { status } = req.body;

  try {
    const appointment = await Appointment.findById(req.params.id);
    if (!appointment) {
      return res.status(404).json({ msg: 'Appointment not found' });
    }

    // Verify authorized user
    if (appointment.doctorId !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(401).json({ msg: 'Not authorized' });
    }

    appointment.status = status;
    if (status === 'IN_PROGRESS') {
      appointment.consultationStartedAt = new Date();
      await QueueItem.findOneAndUpdate(
        { appointmentId: appointment._id.toString() },
        { $set: { status: 'IN_PROGRESS', consultationStartedAt: new Date() } }
      );
    } else if (status === 'COMPLETED') {
      appointment.consultationCompletedAt = new Date();
      await QueueItem.findOneAndUpdate(
        { appointmentId: appointment._id.toString() },
        { $set: { status: 'COMPLETED', isActive: false, consultationCompletedAt: new Date() } }
      );
    }
    await appointment.save();

    if (status === 'CANCELLED') {
      await QueueItem.findOneAndUpdate(
        { appointmentId: appointment._id.toString() },
        { $set: { status: 'CANCELLED', isActive: false } }
      );
    }

    // Always recalculate queue on any status changes to update delay estimations
    await recalculateQueue(appointment.doctorId, appointment.date);

    res.json(appointment);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

router.recalculateQueue = recalculateQueue;
module.exports = router;

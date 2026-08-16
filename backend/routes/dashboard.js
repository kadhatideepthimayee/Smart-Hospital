const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const Notification = require('../models/Notification');
const Appointment = require('../models/Appointment');
const QueueItem = require('../models/QueueItem');
const Activity = require('../models/Activity');
const DoctorProfile = require('../models/DoctorProfile');

// @route   GET api/dashboard/notifications
// @desc    Get all notifications for current user
// @access  Private
router.get('/notifications', auth, async (req, res) => {
  try {
    const notifications = await Notification.find({ userId: req.user.id }).sort({ timestamp: -1 });
    
    const mapped = notifications.map(n => {
      const obj = n.toObject();
      obj.id = obj._id.toString();
      return obj;
    });

    res.json(mapped);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/dashboard/notifications/unread-count
// @desc    Get unread notification count
// @access  Private
router.get('/notifications/unread-count', auth, async (req, res) => {
  try {
    const count = await Notification.countDocuments({ userId: req.user.id, isRead: false });
    res.json({ count });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   PUT api/dashboard/notifications/:id/read
// @desc    Mark a notification as read
// @access  Private
router.put('/notifications/:id/read', auth, async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id);
    if (!notification) {
      return res.status(404).json({ msg: 'Notification not found' });
    }
    notification.isRead = true;
    await notification.save();
    res.json(notification);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   PUT api/dashboard/notifications/read-all
// @desc    Mark all notifications as read for current user
// @access  Private
router.put('/notifications/read-all', auth, async (req, res) => {
  try {
    await Notification.updateMany({ userId: req.user.id, isRead: false }, { $set: { isRead: true } });
    res.json({ msg: 'All notifications marked as read' });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   DELETE api/dashboard/notifications/:id
// @desc    Delete a notification
// @access  Private
router.delete('/notifications/:id', auth, async (req, res) => {
  try {
    await Notification.findOneAndDelete({ _id: req.params.id, userId: req.user.id });
    res.json({ msg: 'Notification deleted' });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/dashboard/upcoming-appointment
// @desc    Get next upcoming appointment
// @access  Private (Patient)
router.get('/upcoming-appointment', auth, async (req, res) => {
  try {
    const startOfToday = new Date(Date.now() - 24 * 60 * 60 * 1000);

    const upcoming = await Appointment.findOne({
      patientId: req.user.id,
      timestamp: { $gte: startOfToday },
      status: { $in: ['UPCOMING', 'IN_PROGRESS'] }
    }).sort({ timestamp: 1 });

    if (!upcoming) {
      return res.json(null);
    }

    res.json({
      appointmentId: upcoming._id.toString(),
      doctorName: upcoming.doctorName,
      department: upcoming.department,
      status: upcoming.status,
      date: upcoming.date,
      time: upcoming.time
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// Copy timeToMinutes helper for parsing time strings
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

// @route   GET api/dashboard/live-queue
// @desc    Get live queue tracking info for patient
// @access  Private (Patient)
router.get('/live-queue', auth, async (req, res) => {
  try {
    let activeQueueItem;
    if (req.query.appointmentId) {
      activeQueueItem = await QueueItem.findOne({
        appointmentId: req.query.appointmentId
      });
    } else {
      // Find the next active/upcoming appointment for today or future (using 24h ago for timezone resilience)
      const startOfToday = new Date(Date.now() - 24 * 60 * 60 * 1000);
      
      const upcoming = await Appointment.findOne({
        patientId: req.user.id,
        timestamp: { $gte: startOfToday },
        status: { $in: ['UPCOMING', 'IN_PROGRESS'] }
      }).sort({ timestamp: 1 });

      if (upcoming) {
        activeQueueItem = await QueueItem.findOne({
          appointmentId: upcoming._id.toString(),
          isActive: true
        });
      }

      // Fallback to finding the oldest active queue item
      if (!activeQueueItem) {
        activeQueueItem = await QueueItem.findOne({
          patientId: req.user.id,
          isActive: true
        }).sort({ timestamp: 1 });
      }
    }

    if (!activeQueueItem) {
      return res.json(null);
    }

    const { doctorId, date } = activeQueueItem;

    // Fetch doctor profile to get working hours and slot duration
    const doctorProfile = await DoctorProfile.findOne({ uid: doctorId });
    const defaultSlotDuration = doctorProfile ? (doctorProfile.slotDuration || 15) : 15;

    // Find all active (non-cancelled) appointments for the doctor on this date
    const appointments = await Appointment.find({
      doctorId,
      date,
      status: { $ne: 'CANCELLED' }
    });

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
    }

    const now = new Date();
    const nowMin = now.getHours() * 60 + now.getMinutes();

    // 2. Timeline simulation
    let timelineMin = doctorProfile ? timeToMinutes(doctorProfile.consultationStartTime) : 540;

    let targetEstimatedWait = 0;
    let targetEstimatedDelay = 0;
    let targetPatientsAhead = 0;

    // Find current serving token
    let currentServingToken = "0";
    const inProgressAppt = appointments.find(a => a.status === 'IN_PROGRESS');
    if (inProgressAppt) {
      currentServingToken = inProgressAppt.tokenNumber;
    } else {
      const completedList = appointments.filter(a => a.status === 'COMPLETED');
      if (completedList.length > 0) {
        currentServingToken = completedList[completedList.length - 1].tokenNumber;
      }
    }

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

      // If this is the patient's appointment
      if (appt._id.toString() === activeQueueItem.appointmentId) {
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

    let crowdLevel = 'LOW';
    if (targetPatientsAhead > 10) crowdLevel = 'HIGH';
    else if (targetPatientsAhead > 4) crowdLevel = 'MEDIUM';

    // Determine status based on actual delay
    let statusText = activeQueueItem.status;
    if (targetEstimatedDelay >= 20 && (statusText === 'WAITING' || statusText === 'UPCOMING')) {
      statusText = 'DOCTOR_RUNNING_LATE';
    }

    res.json({
      isActive: activeQueueItem.isActive,
      queueNumber: activeQueueItem.tokenNumber,
      currentServingToken,
      status: statusText,
      patientsAhead: targetPatientsAhead,
      estimatedWaitMinutes: targetEstimatedWait,
      crowdLevel,
      department: activeQueueItem.department
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/dashboard/activities
// @desc    Get recent 10 activity items
// @access  Private
router.get('/activities', auth, async (req, res) => {
  try {
    const activities = await Activity.find({ userId: req.user.id })
      .sort({ _id: -1 }) // Sort by insertion order descending
      .limit(10);

    const mapped = activities.map(a => {
      const obj = a.toObject();
      obj.id = obj._id.toString();
      return obj;
    });

    res.json(mapped);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/dashboard/activity
// @desc    Create activity log entry
// @access  Private
router.post('/activity', auth, async (req, res) => {
  const { type, title, description } = req.body;

  try {
    // Formatted timestamp string, e.g. "Aug 14, 2026 1:54 PM"
    const timestampStr = new Date().toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      hour12: true
    });

    const newActivity = new Activity({
      userId: req.user.id,
      type,
      title,
      description,
      timestamp: timestampStr
    });

    await newActivity.save();
    res.json(newActivity);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

module.exports = router;

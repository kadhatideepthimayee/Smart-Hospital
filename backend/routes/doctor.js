const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const DoctorProfile = require('../models/DoctorProfile');
const User = require('../models/User');
const QueueItem = require('../models/QueueItem');
const Appointment = require('../models/Appointment');
const Notification = require('../models/Notification');
const Activity = require('../models/Activity');

// @route   GET api/doctors/profile
// @desc    Get current logged in doctor's profile
// @access  Private (Doctor)
router.get('/profile', auth, async (req, res) => {
  try {
    const profile = await DoctorProfile.findOne({ uid: req.user.id });
    if (!profile) {
      return res.status(404).json({ msg: 'Doctor profile not found' });
    }
    res.json(profile);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/doctors/profile/:uid
// @desc    Get specific doctor's profile by UID
// @access  Public/Private
router.get('/profile/:uid', async (req, res) => {
  try {
    const profile = await DoctorProfile.findOne({ uid: req.params.uid });
    if (!profile) {
      return res.status(404).json({ msg: 'Doctor profile not found' });
    }
    res.json(profile);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/doctors/profile/setup
// @desc    Setup or update professional doctor profile
// @access  Private (Doctor)
router.post('/profile/setup', auth, async (req, res) => {
  const {
    qualification,
    department,
    specialization,
    experienceYears,
    registrationAuthority,
    registrationNumber,
    consultationFee,
    bio,
    registrationCertificateUrl,
    verificationDocumentUrl,
    workingDays,
    consultationStartTime,
    consultationEndTime,
    lunchStartTime,
    lunchEndTime,
    breakStartTime,
    breakEndTime,
    slotDuration
  } = req.body;

  try {
    let profile = await DoctorProfile.findOne({ uid: req.user.id });

    // Build profile object
    const profileFields = {
      uid: req.user.id,
      qualification,
      department,
      specialization,
      experienceYears,
      registrationAuthority,
      registrationNumber,
      consultationFee,
      bio,
      registrationCertificateUrl,
      verificationDocumentUrl,
      workingDays,
      consultationStartTime,
      consultationEndTime,
      lunchStartTime,
      lunchEndTime,
      breakStartTime,
      breakEndTime,
      slotDuration,
      verificationStatus: req.body.verificationStatus || 'PENDING', 
      submittedAt: new Date()
    };

    if (profile) {
      // Update
      profile = await DoctorProfile.findOneAndUpdate(
        { uid: req.user.id },
        { $set: profileFields },
        { new: true }
      );
    } else {
      // Create
      profile = new DoctorProfile(profileFields);
      await profile.save();
    }

    // Create notification for admin
    const user = await User.findById(req.user.id);
    const adminNotification = new Notification({
      doctorId: req.user.id,
      title: 'New Verification Request',
      message: `Dr. ${user.fullName} has submitted documents for verification.`,
      type: 'DOCTOR_VERIFICATION'
    });
    await adminNotification.save();

    res.json(profile);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/doctors/verified
// @desc    Get all verified doctors
// @access  Private
router.get('/verified', auth, async (req, res) => {
  try {
    const verifiedDoctors = await DoctorProfile.find({
      verificationStatus: { $in: ['VERIFIED', 'APPROVED'] }
    });
    res.json(verifiedDoctors);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/doctors/availability
// @desc    Set doctor availability parameters
// @access  Private (Doctor)
router.post('/availability', auth, async (req, res) => {
  const {
    workingDays,
    consultationStartTime,
    consultationEndTime,
    lunchStartTime,
    lunchEndTime,
    breakStartTime,
    breakEndTime,
    slotDuration,
    consultationFee
  } = req.body;

  try {
    let profile = await DoctorProfile.findOne({ uid: req.user.id });
    if (!profile) {
      return res.status(404).json({ msg: 'Doctor profile not found' });
    }

    profile.workingDays = workingDays || profile.workingDays;
    profile.consultationStartTime = consultationStartTime || profile.consultationStartTime;
    profile.consultationEndTime = consultationEndTime || profile.consultationEndTime;
    profile.lunchStartTime = lunchStartTime || profile.lunchStartTime;
    profile.lunchEndTime = lunchEndTime || profile.lunchEndTime;
    profile.breakStartTime = breakStartTime || profile.breakStartTime;
    profile.breakEndTime = breakEndTime || profile.breakEndTime;
    profile.slotDuration = slotDuration || profile.slotDuration;
    if (consultationFee !== undefined) {
      profile.consultationFee = consultationFee;
    }

    await profile.save();
    res.json(profile);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

router.get('/queue', auth, async (req, res) => {
  try {
    const getTodayDateString = () => {
      const options = { month: 'short', day: 'numeric', year: 'numeric' };
      const formatted = new Date().toLocaleDateString('en-US', options);
      return formatted;
    };

    const targetDate = req.query.date || getTodayDateString();
    console.log(`[DOCTOR_QUEUE_DEBUG] Fetching queue for doctor: ${req.user.id}, Date: ${targetDate}`);

    const queueItems = await QueueItem.find({
      doctorId: req.user.id,
      date: targetDate,
      isActive: true
    });
    queueItems.sort((a, b) => parseInt(a.tokenNumber || '0') - parseInt(b.tokenNumber || '0'));
    res.json(queueItems);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   PUT api/doctors/queue/:queueId
// @desc    Update a queue entry's status
// @access  Private (Doctor)
router.put('/queue/:queueId', auth, async (req, res) => {
  const { newStatus } = req.body;

  try {
    const queueItem = await QueueItem.findById(req.params.queueId);
    if (!queueItem) {
      return res.status(404).json({ msg: 'Queue entry not found' });
    }

    if (queueItem.doctorId !== req.user.id) {
      return res.status(401).json({ msg: 'Not authorized' });
    }

    queueItem.status = newStatus;
    if (newStatus === 'COMPLETED' || newStatus === 'CANCELLED') {
      queueItem.isActive = false;
    }

    if (newStatus === 'IN_PROGRESS') {
      queueItem.consultationStartedAt = new Date();
      await Appointment.findByIdAndUpdate(queueItem.appointmentId, {
        status: 'IN_PROGRESS',
        consultationStartedAt: new Date()
      });
    } else if (newStatus === 'COMPLETED') {
      queueItem.consultationCompletedAt = new Date();
      await Appointment.findByIdAndUpdate(queueItem.appointmentId, {
        status: 'COMPLETED',
        consultationCompletedAt: new Date()
      });
    }

    await queueItem.save();

    // Recalculate queue chronologically for doctor & date
    // Import recalculateQueue from routes/appointment to trigger updates
    try {
      const { recalculateQueue } = require('./appointment_helper');
      if (recalculateQueue) {
        await recalculateQueue(queueItem.doctorId, queueItem.date);
      }
    } catch (e) {
      // Helper might be in appointment.js directly
      const appointmentsRoute = require('./appointment');
      if (appointmentsRoute && appointmentsRoute.recalculateQueue) {
        await appointmentsRoute.recalculateQueue(queueItem.doctorId, queueItem.date);
      }
    }

    // Send notifications and create logs
    const doctorUser = await User.findById(req.user.id);
    const timestampStr = new Date().toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      hour12: true
    });

    if (newStatus === 'IN_PROGRESS') {
      // 1. Notify patient
      const patientNotification = new Notification({
        userId: queueItem.patientId,
        title: 'Consultation Started',
        message: 'Your doctor has started your consultation. Please proceed to the consultation room.',
        type: 'QUEUE'
      });
      await patientNotification.save();

      // 2. Log for patient
      const patientLog = new Activity({
        userId: queueItem.patientId,
        type: 'QUEUE',
        title: 'Consultation Started',
        description: 'Your consultation has started',
        timestamp: timestampStr
      });
      await patientLog.save();

      // 3. Log for doctor
      const doctorLog = new Activity({
        userId: req.user.id,
        type: 'QUEUE',
        title: 'Started Consultation',
        description: `Started consultation for ${queueItem.patientName} (Token #${queueItem.tokenNumber})`,
        timestamp: timestampStr
      });
      await doctorLog.save();
    } else if (newStatus === 'COMPLETED') {
      // 1. Notify patient
      const patientNotification = new Notification({
        userId: queueItem.patientId,
        title: 'Consultation Completed',
        message: 'Your consultation is complete. Please leave your feedback!',
        type: 'QUEUE'
      });
      await patientNotification.save();

      // 2. Log for patient
      const patientLog = new Activity({
        userId: queueItem.patientId,
        type: 'QUEUE',
        title: 'Consultation Completed',
        description: 'Your consultation has completed',
        timestamp: timestampStr
      });
      await patientLog.save();

      // 3. Log for doctor
      const doctorLog = new Activity({
        userId: req.user.id,
        type: 'QUEUE',
        title: 'Completed Consultation',
        description: `Completed consultation for ${queueItem.patientName}`,
        timestamp: timestampStr
      });
      await doctorLog.save();
    }

    res.json(queueItem);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/doctors/patients
// @desc    Get all unique patients for doctor
// @access  Private (Doctor)
router.get('/patients', auth, async (req, res) => {
  try {
    const appointments = await Appointment.find({ doctorId: req.user.id });
    const patientIds = [...new Set(appointments.map(app => app.patientId))];

    const patients = await User.find({
      _id: { $in: patientIds }
    }).select('-password');

    const normalizedPatients = patients.map(p => ({
      uid: p._id.toString(),
      fullName: p.fullName,
      email: p.email,
      phone: p.phone,
      role: p.role,
      profileImage: p.profileImage,
      status: p.status
    }));

    res.json(normalizedPatients);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

module.exports = router;

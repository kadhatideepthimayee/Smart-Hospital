const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const DoctorFeedback = require('../models/DoctorFeedback');
const Appointment = require('../models/Appointment');
const User = require('../models/User');

// @route   POST api/feedback
// @desc    Submit feedback/rating for a doctor
// @access  Private (Patient)
router.post('/', auth, async (req, res) => {
  const { doctorId, rating, feedback, appointmentId } = req.body;

  try {
    // 1. Verify appointment completion and ownership
    const appointment = await Appointment.findById(appointmentId);
    if (!appointment) {
      return res.status(404).json({ msg: 'Appointment not found' });
    }

    if (appointment.patientId !== req.user.id) {
      return res.status(401).json({ msg: 'Not authorized to review this appointment' });
    }

    if (appointment.status !== 'COMPLETED') {
      return res.status(400).json({ msg: 'You can only review completed appointments' });
    }

    // 2. Check for duplicate review
    const existingFeedback = await DoctorFeedback.findOne({ appointmentId });
    if (existingFeedback) {
      return res.status(400).json({ msg: 'You have already submitted feedback for this appointment' });
    }

    const patientUser = await User.findById(req.user.id);
    const docFeedback = new DoctorFeedback({
      doctorId,
      patientId: req.user.id,
      patientName: patientUser ? patientUser.fullName : 'Patient',
      rating,
      feedback: feedback || "",
      appointmentId
    });

    await docFeedback.save();

    // 3. Create doctor notification
    const Notification = require('../models/Notification');
    const doctorNotification = new Notification({
      userId: doctorId,
      title: 'New Feedback Received',
      message: `A patient has left a ${rating}-star review for your consultation.`,
      type: 'FEEDBACK'
    });
    await doctorNotification.save();

    res.json(docFeedback);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/feedback/doctor/:doctorId
// @desc    Get all reviews/feedbacks for a specific doctor
// @access  Public/Private
router.get('/doctor/:doctorId', auth, async (req, res) => {
  try {
    const feedbacks = await DoctorFeedback.find({ doctorId: req.params.doctorId }).sort({ createdAt: -1 });
    res.json(feedbacks);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/feedback/appointment/:appointmentId
// @desc    Check if feedback exists for an appointment
// @access  Private
router.get('/appointment/:appointmentId', auth, async (req, res) => {
  try {
    const feedback = await DoctorFeedback.findOne({ appointmentId: req.params.appointmentId });
    res.json({ exists: !!feedback, feedback });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

module.exports = router;

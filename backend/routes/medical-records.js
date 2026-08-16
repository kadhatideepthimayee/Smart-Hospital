const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const MedicalRecord = require('../models/MedicalRecord');
const Appointment = require('../models/Appointment');
const User = require('../models/User');

// @route   POST api/medical-records
// @desc    Create a new medical record
// @access  Private (Doctor)
router.post('/', auth, async (req, res) => {
  const { appointmentId, patientId, diagnosis, prescription, notes, followUpDate } = req.body;

  try {
    // 1. Fetch appointment details to verify permissions
    const appointment = await Appointment.findById(appointmentId);
    if (!appointment) {
      return res.status(404).json({ msg: 'Appointment not found' });
    }

    if (appointment.doctorId !== req.user.id) {
      return res.status(401).json({ msg: 'Not authorized as the doctor of this appointment' });
    }

    const record = new MedicalRecord({
      patientId,
      patientName: appointment.patientName,
      doctorId: req.user.id,
      doctorName: appointment.doctorName,
      appointmentId,
      diagnosis,
      prescription,
      notes: notes || "",
      followUpDate: followUpDate || ""
    });

    await record.save();

    // Send patient a notification about new medical record
    const Notification = require('../models/Notification');
    const patientNotification = new Notification({
      userId: patientId,
      title: 'New Medical Record Available',
      message: `Dr. ${appointment.doctorName} has added a medical record for your consultation.`,
      type: 'MEDICAL_RECORD'
    });
    await patientNotification.save();

    res.json(record);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/medical-records/patient
// @desc    Get all medical records for the authenticated patient
// @access  Private (Patient)
router.get('/patient', auth, async (req, res) => {
  try {
    const records = await MedicalRecord.find({ patientId: req.user.id }).sort({ createdAt: -1 });
    res.json(records);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/medical-records/:id
// @desc    Get a single medical record detail
// @access  Private
router.get('/:id', auth, async (req, res) => {
  try {
    const record = await MedicalRecord.findById(req.params.id);
    if (!record) {
      return res.status(404).json({ msg: 'Medical record not found' });
    }

    // Verify authorized user
    if (record.patientId !== req.user.id && record.doctorId !== req.user.id && req.user.role !== 'ADMIN') {
      return res.status(401).json({ msg: 'Not authorized to view this record' });
    }

    res.json(record);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

module.exports = router;

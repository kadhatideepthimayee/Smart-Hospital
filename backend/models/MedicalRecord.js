const mongoose = require('mongoose');

const MedicalRecordSchema = new mongoose.Schema({
  patientId: { type: String, required: true },
  patientName: { type: String, required: true },
  doctorId: { type: String, required: true },
  doctorName: { type: String, required: true },
  appointmentId: { type: String, required: true },
  diagnosis: { type: String, required: true },
  prescription: { type: String, required: true },
  notes: { type: String, default: "" },
  followUpDate: { type: String, default: "" },
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('MedicalRecord', MedicalRecordSchema);

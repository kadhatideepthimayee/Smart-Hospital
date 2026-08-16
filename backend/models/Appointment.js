const mongoose = require('mongoose');

const AppointmentSchema = new mongoose.Schema({
  patientId: { type: String, required: true },
  patientName: { type: String, required: true },
  doctorId: { type: String, required: true },
  doctorName: { type: String, required: true },
  department: { type: String, default: "" },
  date: { type: String, required: true }, // Format: YYYY-MM-DD
  time: { type: String, required: true }, // Format: HH:MM
  status: { type: String, enum: ['UPCOMING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'], default: 'UPCOMING' },
  tokenNumber: { type: String, default: null },
  createdAt: { type: Date, default: Date.now },
  timestamp: { type: Date }, // Date object constructed from date & time
  consultationStartedAt: { type: Date },
  consultationCompletedAt: { type: Date }
});

module.exports = mongoose.model('Appointment', AppointmentSchema);

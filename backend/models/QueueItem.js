const mongoose = require('mongoose');

const QueueItemSchema = new mongoose.Schema({
  appointmentId: { type: String, required: true },
  doctorId: { type: String, required: true },
  patientId: { type: String, required: true },
  patientName: { type: String, required: true },
  tokenNumber: { type: String, required: true },
  status: { type: String, enum: ['WAITING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'], default: 'WAITING' },
  department: { type: String, default: null },
  date: { type: String, default: "" }, // Format: e.g. "Aug 17, 2026"
  isActive: { type: Boolean, default: true },
  timestamp: { type: Date, default: Date.now },
  estimatedWaitMinutes: { type: Number, default: 0 },
  consultationStartedAt: { type: Date },
  consultationCompletedAt: { type: Date },
  lastNotifiedWaitMinutes: { type: Number, default: 0 },
  lastNotifiedPosition: { type: Number, default: -1 }
});

module.exports = mongoose.model('QueueItem', QueueItemSchema);

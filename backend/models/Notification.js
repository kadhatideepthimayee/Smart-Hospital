const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema({
  userId: { type: String, default: null }, // Null if it is an Admin Notification
  doctorId: { type: String, default: null }, // Relevant if notification is about a doctor (e.g. for admin)
  title: { type: String, required: true },
  message: { type: String, required: true },
  type: { type: String, default: 'GENERAL' }, // GENERAL, APPOINTMENT, VERIFICATION, SYSTEM, DOCTOR_VERIFICATION
  isRead: { type: Boolean, default: false },
  timestamp: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Notification', NotificationSchema);

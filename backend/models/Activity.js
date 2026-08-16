const mongoose = require('mongoose');

const ActivitySchema = new mongoose.Schema({
  userId: { type: String, required: true },
  type: { type: String, required: true }, // e.g., GENERAL, APPOINTMENT, VERIFICATION
  title: { type: String, required: true },
  description: { type: String, required: true },
  timestamp: { type: String, required: true } // Formatted string, e.g. "Aug 14, 2026 1:54 PM"
});

module.exports = mongoose.model('Activity', ActivitySchema);

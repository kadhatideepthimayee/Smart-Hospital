const mongoose = require('mongoose');

const DoctorProfileSchema = new mongoose.Schema({
  uid: { type: String, required: true, unique: true },
  fullName: { type: String, default: "" },
  email: { type: String, default: "" },
  phone: { type: String, default: "" },
  qualification: { type: String, default: "" },
  department: { type: String, default: "" },
  specialization: { type: String, default: "" },
  experienceYears: { type: Number, default: 0 },
  registrationAuthority: { type: String, default: "" },
  registrationNumber: { type: String, default: "" },
  consultationFee: { type: Number, default: 0.0 },
  bio: { type: String, default: "" },
  profileImage: { type: String, default: "" },
  registrationCertificateUrl: { type: String, default: "" },
  verificationDocumentUrl: { type: String, default: "" },
  workingDays: [{ type: String }],
  consultationStartTime: { type: String, default: "" },
  consultationEndTime: { type: String, default: "" },
  lunchStartTime: { type: String, default: "" },
  lunchEndTime: { type: String, default: "" },
  breakStartTime: { type: String, default: "" },
  breakEndTime: { type: String, default: "" },
  slotDuration: { type: Number, default: 15 },
  verificationStatus: { type: String, enum: ['DRAFT', 'PENDING', 'VERIFIED', 'APPROVED', 'REJECTED'], default: 'DRAFT' },
  submittedAt: { type: Date, default: Date.now },
  reviewedAt: { type: Date },
  reviewedBy: { type: String },
  rejectionReason: { type: String, default: null }
});

module.exports = mongoose.model('DoctorProfile', DoctorProfileSchema);

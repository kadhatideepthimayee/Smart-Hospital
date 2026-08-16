const express = require('express');
const router = express.Router();
const auth = require('../middleware/auth');
const DoctorProfile = require('../models/DoctorProfile');
const User = require('../models/User');
const Notification = require('../models/Notification');

// Middleware to check if user is admin
const adminCheck = (req, res, next) => {
  if (req.user.role !== 'ADMIN') {
    return res.status(403).json({ msg: 'Access denied: Admin role required' });
  }
  next();
};

// @route   GET api/admin/doctors
// @desc    Get doctors by verification status
// @access  Private (Admin)
router.get('/doctors', [auth, adminCheck], async (req, res) => {
  const status = req.query.status;

  try {
    let query = {};
    if (status === 'VERIFIED') {
      query.verificationStatus = { $in: ['VERIFIED', 'APPROVED'] };
    } else if (status) {
      query.verificationStatus = status;
    }

    const doctors = await DoctorProfile.find(query).sort({ submittedAt: -1 });
    res.json(doctors);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/admin/doctors/all
// @desc    Get all doctor profiles
// @access  Private (Admin)
router.get('/doctors/all', [auth, adminCheck], async (req, res) => {
  try {
    const doctors = await DoctorProfile.find().sort({ submittedAt: -1 });
    res.json(doctors);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/admin/verify-doctor
// @desc    Approve or Reject doctor verification profile
// @access  Private (Admin)
router.post('/verify-doctor', [auth, adminCheck], async (req, res) => {
  const { doctorId, newStatus, rejectionReason } = req.body;

  try {
    let profile = await DoctorProfile.findOne({ uid: doctorId });
    if (!profile) {
      return res.status(404).json({ msg: 'Doctor profile not found' });
    }

    profile.verificationStatus = newStatus;
    profile.reviewedAt = new Date();
    profile.reviewedBy = req.user.id;
    if (rejectionReason) {
      profile.rejectionReason = rejectionReason;
    }
    await profile.save();

    // Create notifications for the doctor
    let title = 'Verification Status Updated';
    let message = '';
    
    if (newStatus === 'VERIFIED' || newStatus === 'APPROVED') {
      message = 'Congratulations! Your professional profile has been verified by the administrator.';
    } else if (newStatus === 'REJECTED') {
      message = `Your professional profile was rejected. Reason: ${rejectionReason || 'Invalid credentials'}.`;
    }

    const userNotification = new Notification({
      userId: doctorId,
      title,
      message,
      type: 'VERIFICATION'
    });
    await userNotification.save();

    res.json({ msg: `Doctor profile status updated to ${newStatus}` });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/admin/notifications
// @desc    Get all admin notifications (notifications with userId = null or type DOCTOR_VERIFICATION)
// @access  Private (Admin)
router.get('/notifications', [auth, adminCheck], async (req, res) => {
  try {
    const notifications = await Notification.find({
      $or: [
        { userId: null },
        { type: 'DOCTOR_VERIFICATION' }
      ]
    }).sort({ timestamp: -1 });

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

// @route   DELETE api/admin/notifications/:id
// @desc    Delete a notification by ID
// @access  Private (Admin)
router.delete('/notifications/:id', [auth, adminCheck], async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id);
    if (!notification) {
      return res.status(404).json({ msg: 'Notification not found' });
    }
    await Notification.findByIdAndDelete(req.params.id);
    res.json({ msg: 'Notification removed' });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   PUT api/admin/notifications/:id/read
// @desc    Mark admin notification as read
// @access  Private (Admin)
router.put('/notifications/:id/read', [auth, adminCheck], async (req, res) => {
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

// @route   GET api/admin/notifications/unread-count
// @desc    Get count of unread admin notifications
// @access  Private (Admin)
router.get('/notifications/unread-count', [auth, adminCheck], async (req, res) => {
  try {
    const count = await Notification.countDocuments({
      $or: [
        { userId: null },
        { type: 'DOCTOR_VERIFICATION' }
      ],
      isRead: false
    });
    res.json({ count });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

module.exports = router;

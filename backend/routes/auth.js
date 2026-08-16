const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const auth = require('../middleware/auth');
const User = require('../models/User');
const DoctorProfile = require('../models/DoctorProfile');

// @route   POST api/auth/register
// @desc    Register a user
// @access  Public
router.post('/register', async (req, res) => {
  const { fullName, email, phone, password, role } = req.body;

  try {
    let user = await User.findOne({ email });

    if (user) {
      return res.status(400).json({ msg: 'Email is already registered. Please login.' });
    }

    const adminEmail = (process.env.ADMIN_EMAIL || 'kadhatideepthimayee@gmail.com').toLowerCase();
    const finalRole = email.toLowerCase() === adminEmail ? 'ADMIN' : role;

    user = new User({
      fullName,
      email,
      phone,
      password,
      role: finalRole
    });

    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(password, salt);

    await user.save();

    // If role is DOCTOR, create initial DoctorProfile document
    if (role === 'DOCTOR') {
      const initialProfile = new DoctorProfile({
        uid: user._id.toString(),
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        verificationStatus: 'DRAFT'
      });
      await initialProfile.save();
    }

    // Generate JWT token on registration success (so they are automatically logged in, just like Firebase Auth behavior)
    const payload = {
      user: {
        id: user._id.toString(),
        role: user.role
      }
    };

    jwt.sign(
      payload,
      process.env.JWT_SECRET || 'medplus_secret_key_987654321_secure',
      { expiresIn: 360000 },
      (err, token) => {
        if (err) throw err;
        res.json({
          token,
          user: {
            uid: user._id.toString(),
            fullName: user.fullName,
            email: user.email,
            phone: user.phone,
            role: user.role,
            profileImage: user.profileImage,
            status: user.status
          }
        });
      }
    );
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/auth/login
// @desc    Authenticate user & get token
// @access  Public
router.post('/login', async (req, res) => {
  const { email, password } = req.body;

  try {
    let user = await User.findOne({ email });

    if (!user) {
      return res.status(400).json({ msg: 'No account found with this email.' });
    }

    const isMatch = await bcrypt.compare(password, user.password);

    if (!isMatch) {
      return res.status(400).json({ msg: 'Invalid email or password.' });
    }

    const payload = {
      user: {
        id: user._id.toString(),
        role: user.role
      }
    };

    jwt.sign(
      payload,
      process.env.JWT_SECRET || 'medplus_secret_key_987654321_secure',
      { expiresIn: 360000 },
      (err, token) => {
        if (err) throw err;
        res.json({
          token,
          user: {
            uid: user._id.toString(),
            fullName: user.fullName,
            email: user.email,
            phone: user.phone,
            role: user.role,
            profileImage: user.profileImage,
            status: user.status
          }
        });
      }
    );
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   GET api/auth/me
// @desc    Get logged in user
// @access  Private
router.get('/me', auth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select('-password');
    res.json({
      uid: user._id.toString(),
      fullName: user.fullName,
      email: user.email,
      phone: user.phone,
      role: user.role,
      profileImage: user.profileImage,
      status: user.status
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   PUT api/auth/profile
// @desc    Update user profile
// @access  Private
router.put('/profile', auth, async (req, res) => {
  const { fullName, phone } = req.body;

  try {
    let user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ msg: 'User not found' });
    }

    user.fullName = fullName || user.fullName;
    user.phone = phone || user.phone;
    await user.save();

    // If DOCTOR, also update details in DoctorProfile
    if (user.role === 'DOCTOR') {
      let doctorProfile = await DoctorProfile.findOne({ uid: user._id.toString() });
      if (doctorProfile) {
        doctorProfile.fullName = user.fullName;
        doctorProfile.phone = user.phone;
        await doctorProfile.save();
      }
    }

    res.json({
      uid: user._id.toString(),
      fullName: user.fullName,
      email: user.email,
      phone: user.phone,
      role: user.role,
      profileImage: user.profileImage,
      status: user.status
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/auth/google
// @desc    Authenticate with Google ID Token (Login/Register)
// @access  Public
router.post('/google', async (req, res) => {
  const { idToken, role } = req.body;

  if (!idToken) {
    return res.status(400).json({ msg: 'Please provide Google ID token' });
  }

  try {
    // 1. Verify token with Google API using native https module
    const https = require('https');
    const payload = await new Promise((resolve, reject) => {
      https.get(`https://oauth2.googleapis.com/tokeninfo?id_token=${idToken}`, (googleRes) => {
        let data = '';
        googleRes.on('data', (chunk) => { data += chunk; });
        googleRes.on('end', () => {
          if (googleRes.statusCode === 200) {
            try {
              resolve(JSON.parse(data));
            } catch (e) {
              reject(new Error('Failed to parse Google response'));
            }
          } else {
            reject(new Error('Google verification failed'));
          }
        });
      }).on('error', (err) => {
        reject(err);
      });
    });

    const { email, name, picture } = payload;

    if (!email) {
      return res.status(400).json({ msg: 'Google token does not contain email' });
    }

    // 2. Check if user exists
    let user = await User.findOne({ email });

    const adminEmail = (process.env.ADMIN_EMAIL || 'kadhatideepthimayee@gmail.com').toLowerCase();
    const isSystemAdmin = email.toLowerCase() === adminEmail;

    if (!user) {
      // Create new user
      const selectedRole = isSystemAdmin ? 'ADMIN' : (role || 'PATIENT'); // default fallback
      const salt = await bcrypt.genSalt(10);
      const hashedPassword = await bcrypt.hash('google_oauth_bypass_secret_998877', salt);

      user = new User({
        fullName: name || 'Google User',
        email,
        password: hashedPassword,
        role: selectedRole,
        phone: '',
        profileImage: picture || ''
      });
      await user.save();

      // If role is DOCTOR, create initial DoctorProfile document
      if (selectedRole === 'DOCTOR') {
        const initialProfile = new DoctorProfile({
          uid: user._id.toString(),
          fullName: user.fullName,
          email: user.email,
          phone: '',
          verificationStatus: 'DRAFT'
        });
        await initialProfile.save();
      }
    } else if (isSystemAdmin && user.role !== 'ADMIN') {
      // Automatically elevate to ADMIN if designated as adminEmail
      user.role = 'ADMIN';
      await user.save();
      console.log(`[AUTH] User elevated to ADMIN based on adminEmail: ${email}`);
    }

    // 3. Generate JWT Token
    const jwtPayload = {
      user: {
        id: user.id,
        role: user.role
      }
    };

    const tokenSecret = process.env.JWT_SECRET;
    const localToken = jwt.sign(jwtPayload, tokenSecret, { expiresIn: '7d' });

    res.json({
      token: localToken,
      user: {
        uid: user.id,
        fullName: user.fullName,
        email: user.email,
        role: user.role,
        phone: user.phone || '',
        profileImage: user.profileImage || ''
      }
    });

  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/auth/forgot-password
// @desc    Generate password reset code
// @access  Public
router.post('/forgot-password', async (req, res) => {
  const { email } = req.body;

  try {
    const user = await User.findOne({ email: email.toLowerCase().trim() });
    if (!user) {
      return res.status(400).json({ msg: 'No account found with this email address.' });
    }

    // Generate user-friendly 6-digit numeric reset PIN
    const resetPin = Math.floor(100000 + Math.random() * 900000).toString();
    
    // Hash PIN for secure storage
    const crypto = require('crypto');
    const hashedPin = crypto.createHash('sha256').update(resetPin).digest('hex');

    user.resetPasswordToken = hashedPin;
    user.resetPasswordExpires = Date.now() + 15 * 60 * 1000; // 15 minutes expiry
    await user.save();

    console.log(`\n==================================================`);
    console.log(`[PASSWORD RESET] Email: ${email}`);
    console.log(`[PASSWORD RESET] Reset Code: ${resetPin}`);
    console.log(`==================================================\n`);

    res.json({
      msg: 'A password reset code has been sent to your email.',
      debugPin: resetPin // Included for easy offline testing/prototyping
    });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/auth/verify-reset-code
// @desc    Verify password reset code
// @access  Public
router.post('/verify-reset-code', async (req, res) => {
  const { email, code } = req.body;

  try {
    const crypto = require('crypto');
    const hashedPin = crypto.createHash('sha256').update(code.trim()).digest('hex');

    const user = await User.findOne({
      email: email.toLowerCase().trim(),
      resetPasswordToken: hashedPin,
      resetPasswordExpires: { $gt: Date.now() }
    });

    if (!user) {
      return res.status(400).json({ msg: 'Invalid or expired reset code.' });
    }

    res.json({ msg: 'Reset code verified successfully.' });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

// @route   POST api/auth/reset-password
// @desc    Reset password using verified code
// @access  Public
router.post('/reset-password', async (req, res) => {
  const { email, code, newPassword } = req.body;

  try {
    const crypto = require('crypto');
    const hashedPin = crypto.createHash('sha256').update(code.trim()).digest('hex');

    const user = await User.findOne({
      email: email.toLowerCase().trim(),
      resetPasswordToken: hashedPin,
      resetPasswordExpires: { $gt: Date.now() }
    });

    if (!user) {
      return res.status(400).json({ msg: 'Invalid or expired reset code.' });
    }

    // Hash and update password
    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(newPassword, salt);
    
    // Clear reset token fields
    user.resetPasswordToken = null;
    user.resetPasswordExpires = null;
    await user.save();

    res.json({ msg: 'Your password has been successfully reset.' });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server error');
  }
});

module.exports = router;

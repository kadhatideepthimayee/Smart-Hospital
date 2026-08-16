const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// Basic Route for test
app.get('/', (req, res) => {
  res.send('MedPlus Server is running...');
});

// Define Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/doctors', require('./routes/doctor'));
app.use('/api/appointments', require('./routes/appointment'));
app.use('/api/admin', require('./routes/admin'));
app.use('/api/dashboard', require('./routes/dashboard'));
app.use('/api/medical-records', require('./routes/medical-records'));
app.use('/api/feedback', require('./routes/feedback'));

// Connect Database
const dbURI = process.env.MONGODB_URI;
console.log('Connecting to MongoDB Atlas...');

const seedAdminUser = async () => {
  try {
    const User = require('./models/User');
    const bcrypt = require('bcryptjs');

    const adminEmail = process.env.ADMIN_EMAIL || 'admin@medplus.com';
    const adminPassword = process.env.ADMIN_PASSWORD || 'admin123';

    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(adminPassword, salt);

    const existingAdmin = await User.findOne({ email: adminEmail.toLowerCase() });
    if (!existingAdmin) {
      const admin = new User({
        fullName: 'System Administrator',
        email: adminEmail.toLowerCase(),
        password: hashedPassword,
        role: 'ADMIN',
        phone: '1234567890'
      });

      await admin.save();
      console.log(`[SEED] Default Admin user created: ${adminEmail}`);
    } else {
      existingAdmin.password = hashedPassword;
      existingAdmin.role = 'ADMIN';
      await existingAdmin.save();
      console.log(`[SEED] Admin user credentials updated/verified: ${adminEmail}`);
    }
  } catch (err) {
    console.error('[SEED] Failed to seed Admin user:', err.message);
  }
};

mongoose
  .connect(dbURI)
  .then(() => {
    console.log('MongoDB Connected Successfully!');
    seedAdminUser();
  })
  .catch(err => {
    console.error('MongoDB connection error:', err.message);
    process.exit(1);
  });

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server started on port ${PORT}`);
});

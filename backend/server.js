const express = require('express');
const mongoose = require('mongoose');
require('./middleware/localDbFallback');
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

const connectDB = async () => {
  try {
    await mongoose.connect(dbURI);
    console.log('MongoDB Connected Successfully to MongoDB Atlas!');
    seedAdminUser();
  } catch (err) {
    console.error('========================================================================');
    console.error('MongoDB Atlas Connection Error:', err.message);
    console.error('Attempting fallback to local MongoDB (mongodb://127.0.0.1:27017/medplus)...');
    
    const localURI = 'mongodb://127.0.0.1:27017/medplus';
    try {
      await mongoose.connect(localURI);
      console.log('MongoDB Connected Successfully to local MongoDB!');
      seedAdminUser();
    } catch (localErr) {
      console.error('Local MongoDB Connection also failed:', localErr.message);
      console.log('MongoDB Atlas and local servers are unavailable.');
      console.log('Falling back to local JSON file-based database...');
      await seedAdminUser();
      console.error('========================================================================');
      console.error('Keep-Alive: The server is kept running so the clients do not encounter');
      console.error('a raw Network Connection Error. Fallback local JSON database is active.');
      console.error('TO FIX DATABASE SERVERS:');
      console.error('1. Whitelist your current IP address in your MongoDB Atlas Dashboard:');
      console.error('   https://www.mongodb.com/docs/atlas/security-whitelist/');
      console.error('2. Alternatively, install and run MongoDB locally:');
      console.error('   MONGODB_URI=mongodb://127.0.0.1:27017/medplus');
      console.error('========================================================================');
    }
  }
};

connectDB();

const PORT = process.env.PORT || 5000;
const server = app.listen(PORT, () => {
  console.log(`Server started on port ${PORT}`);
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error('========================================================================');
    console.error(`ERROR: Port ${PORT} is already in use by another process.`);
    console.error('This prevents the MedPlus backend from starting, causing "Network Errors"');
    console.error('in both the web and Android applications.');
    console.error('');
    console.error('TO FIX THIS:');
    console.error('1. Open command prompt (cmd) as Administrator.');
    console.error(`2. Run: netstat -ano | findstr :${PORT}`);
    console.error('3. Note the PID (the number on the far right).');
    console.error('4. Kill that process: taskkill /PID <PID> /F');
    console.error('5. Restart this script.');
    console.error('========================================================================');
    process.exit(1);
  } else {
    console.error('Server error:', err);
  }
});

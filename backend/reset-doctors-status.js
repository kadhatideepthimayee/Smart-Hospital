const mongoose = require('mongoose');
require('dotenv').config();

const dbURI = process.env.MONGODB_URI;
console.log('Connecting to MongoDB Atlas...');

const DoctorProfile = require('./models/DoctorProfile');

mongoose.connect(dbURI)
  .then(async () => {
    console.log('Connected!');
    const result = await DoctorProfile.updateMany({}, { $set: { verificationStatus: 'DRAFT' } });
    console.log(`Updated ${result.modifiedCount} doctor profiles back to DRAFT.`);
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });

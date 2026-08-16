const mongoose = require('mongoose');
require('dotenv').config();

const dbURI = process.env.MONGODB_URI;
console.log('Connecting to MongoDB Atlas...');

const DoctorProfile = require('./models/DoctorProfile');

mongoose.connect(dbURI)
  .then(async () => {
    console.log('Connected!');
    const profiles = await DoctorProfile.find({});
    console.log(`Found ${profiles.length} profiles.`);
    profiles.forEach(p => {
      console.log(`\nUID: ${p.uid}`);
      console.log(`Name: ${p.fullName}`);
      console.log(`Cert URL length: ${p.registrationCertificateUrl ? p.registrationCertificateUrl.length : 0}`);
      console.log(`Doc URL length: ${p.verificationDocumentUrl ? p.verificationDocumentUrl.length : 0}`);
      console.log(`Cert URL starts with: ${p.registrationCertificateUrl ? p.registrationCertificateUrl.substring(0, 50) : 'N/A'}`);
      console.log(`Doc URL starts with: ${p.verificationDocumentUrl ? p.verificationDocumentUrl.substring(0, 50) : 'N/A'}`);
    });
    process.exit(0);
  })
  .catch(err => {
    console.error(err);
    process.exit(1);
  });

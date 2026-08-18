const mongoose = require('mongoose');

const dbURI = 'mongodb+srv://kadhatideepthimayee_db_user:v4xyYkVN3bnYwgKG@medpluscluster.w04ghpo.mongodb.net/medplus?retryWrites=true&w=majority';

console.log('Testing connection to MongoDB Atlas cluster...');
console.log('URI:', dbURI.replace(/:([^@]+)@/, ':****@')); // Hide password in logs

mongoose.connect(dbURI, { serverSelectionTimeoutMS: 5000 })
  .then(() => {
    console.log('SUCCESS: Connected to MongoDB Atlas cluster successfully!');
    process.exit(0);
  })
  .catch(err => {
    console.error('FAILURE: Connection failed!');
    console.error('Error Code:', err.code);
    console.error('Error Message:', err.message);
    process.exit(1);
  });

const mongoose = require('mongoose');
require('dotenv').config();

const dbUri = process.env.MONGO_URI || 'mongodb://localhost:27017/medplus';
console.log('Connecting to database:', dbUri);

mongoose.connect(dbUri)
  .then(async () => {
    console.log('Connected to MongoDB!');
    
    const Appointment = require('./models/Appointment');
    const QueueItem = require('./models/QueueItem');

    // Find all appointments
    const appts = await Appointment.find({}).sort({ createdAt: -1 }).limit(5);
    console.log('\n--- LATEST 5 APPOINTMENTS ---');
    appts.forEach(a => {
      console.log(`ID: ${a._id}, Patient: ${a.patientName} (${a.patientId}), Doctor: ${a.doctorName}, Date: ${a.date}, Time: ${a.time}, Status: ${a.status}, Timestamp: ${a.timestamp}, Created: ${a.createdAt}`);
    });

    // Find all queue items
    const items = await QueueItem.find({}).sort({ timestamp: -1 }).limit(5);
    console.log('\n--- LATEST 5 QUEUE ITEMS ---');
    items.forEach(q => {
      console.log(`ID: ${q._id}, ApptID: ${q.appointmentId}, Patient: ${q.patientName} (${q.patientId}), Status: ${q.status}, Active: ${q.isActive}, Date: ${q.date}, Timestamp: ${q.timestamp}`);
    });

    // Run our query logic
    const patientId = appts.length > 0 ? appts[0].patientId : null;
    if (patientId) {
      console.log(`\nTesting query for Patient ID: ${patientId}`);
      const startOfToday = new Date(Date.now() - 24 * 60 * 60 * 1000);
      console.log('startOfToday (24h ago):', startOfToday.toISOString());
      console.log('startOfToday:', startOfToday.toISOString());

      const upcoming = await Appointment.findOne({
        patientId,
        timestamp: { $gte: startOfToday },
        status: { $in: ['UPCOMING', 'IN_PROGRESS'] }
      }).sort({ timestamp: 1 });

      console.log('Found upcoming appointment:', upcoming ? `ID: ${upcoming._id}, Date: ${upcoming.date}, Time: ${upcoming.time}, Status: ${upcoming.status}, Timestamp: ${upcoming.timestamp}` : 'None');

      if (upcoming) {
        const queueItem = await QueueItem.findOne({
          appointmentId: upcoming._id.toString(),
          isActive: true
        });
        console.log('Found corresponding QueueItem:', queueItem ? `ID: ${queueItem._id}, Status: ${queueItem.status}, Active: ${queueItem.isActive}` : 'None');
      }
    }

    mongoose.disconnect();
  })
  .catch(err => {
    console.error('Connection error:', err);
  });

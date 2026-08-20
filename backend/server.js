const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const { 
  createUserWithEmailAndPassword, 
  signInWithEmailAndPassword 
} = require('firebase/auth');
const { db, auth, adminDb, usingAdmin } = require('./firebaseConfig');

const app = express();
const PORT = process.env.PORT || 5000;
const JWT_SECRET = process.env.JWT_SECRET || 'medplus_super_secret_jwt_key_123!';

app.use(cors());
app.use(express.json({ limit: '10mb' })); // Allow Base64 uploads

app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
  next();
});

// Helper to convert time format (e.g. "09:00 AM" or "14:30") to minutes from midnight
const timeToMinutes = (timeStr) => {
  if (!timeStr) return 0;
  const cleanTime = timeStr.trim();
  const timeParts = cleanTime.split(" ");
  const hm = timeParts[0].split(":");
  let hour = parseInt(hm[0]);
  const minute = parseInt(hm[1]);
  if (timeParts.length > 1) {
    const ampm = timeParts[1].toUpperCase();
    if (ampm === "PM" && hour < 12) hour += 12;
    if (ampm === "AM" && hour === 12) hour = 0;
  }
  return hour * 60 + minute;
};

// Helper to recursively convert Firestore Timestamps to ISO strings in response data
const sanitizeTimestamps = (obj) => {
  if (obj === null || obj === undefined) return obj;
  
  if (typeof obj.toDate === 'function') {
    return obj.toDate().toISOString();
  }
  
  if (typeof obj === 'object' && obj.seconds !== undefined && obj.nanoseconds !== undefined) {
    return new Date(obj.seconds * 1000 + Math.floor(obj.nanoseconds / 1000000)).toISOString();
  }
  
  if (Array.isArray(obj)) {
    return obj.map(sanitizeTimestamps);
  }
  
  if (typeof obj === 'object') {
    const newObj = {};
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        newObj[key] = sanitizeTimestamps(obj[key]);
      }
    }
    return newObj;
  }
  
  return obj;
};

// ----------------------------------------------------
// DATABASE ABSTRACTION HELPERS
// ----------------------------------------------------

const getDocHelper = async (collectionName, docId) => {
  if (usingAdmin) {
    const snap = await adminDb.collection(collectionName).doc(docId).get();
    return snap.exists ? sanitizeTimestamps({ id: snap.id, uid: snap.id, ...snap.data() }) : null;
  } else {
    const { doc, getDoc } = require('firebase/firestore');
    const snap = await getDoc(doc(db, collectionName, docId));
    return snap.exists() ? sanitizeTimestamps({ id: snap.id, uid: snap.id, ...snap.data() }) : null;
  }
};

const setDocHelper = async (collectionName, docId, data) => {
  if (usingAdmin) {
    await adminDb.collection(collectionName).doc(docId).set(data);
  } else {
    const { doc, setDoc } = require('firebase/firestore');
    await setDoc(doc(db, collectionName, docId), data);
  }
};

const updateDocHelper = async (collectionName, docId, updates) => {
  if (usingAdmin) {
    await adminDb.collection(collectionName).doc(docId).update(updates);
  } else {
    const { doc, updateDoc } = require('firebase/firestore');
    await updateDoc(doc(db, collectionName, docId), updates);
  }
};

const addDocHelper = async (collectionName, data) => {
  if (usingAdmin) {
    const ref = await adminDb.collection(collectionName).add(data);
    return ref.id;
  } else {
    const { collection, addDoc } = require('firebase/firestore');
    const ref = await addDoc(collection(db, collectionName), data);
    return ref.id;
  }
};

const deleteDocHelper = async (collectionName, docId) => {
  if (usingAdmin) {
    await adminDb.collection(collectionName).doc(docId).delete();
  } else {
    const { doc, deleteDoc } = require('firebase/firestore');
    await deleteDoc(doc(db, collectionName, docId));
  }
};

const queryCollectionHelper = async (collectionName, filters = []) => {
  if (usingAdmin) {
    let q = adminDb.collection(collectionName);
    filters.forEach(f => {
      q = q.where(f.field, f.operator, f.value);
    });
    const snap = await q.get();
    const list = [];
    snap.forEach(docSnap => {
      list.push(sanitizeTimestamps({ id: docSnap.id, uid: docSnap.id, ...docSnap.data() }));
    });
    return list;
  } else {
    const { collection, query, where, getDocs } = require('firebase/firestore');
    let q = collection(db, collectionName);
    if (filters.length > 0) {
      const wheres = filters.map(f => where(f.field, f.operator, f.value));
      q = query(q, ...wheres);
    }
    const snap = await getDocs(q);
    const list = [];
    snap.forEach(docSnap => {
      list.push(sanitizeTimestamps({ id: docSnap.id, uid: docSnap.id, ...docSnap.data() }));
    });
    return list;
  }
};

// ----------------------------------------------------
// AUTHENTICATION ENDPOINTS
// ----------------------------------------------------

app.post('/api/auth/register', async (req, res) => {
  const { fullName, email, phone, password, role } = req.body;
  if (!fullName || !email || !password || !role) {
    return res.status(400).json({ error: 'Missing required registration fields' });
  }

  try {
    const authResult = await createUserWithEmailAndPassword(auth, email, password);
    const uid = authResult.user.uid;
    const createdAt = new Date().toISOString();

    await setDocHelper('users', uid, {
      uid,
      fullName,
      email,
      phone: phone || '',
      role: role.toUpperCase(),
      profileImage: '',
      status: 'ACTIVE',
      createdAt
    });

    if (role.toUpperCase() === 'DOCTOR') {
      await setDocHelper('doctor_profiles', uid, {
        uid,
        fullName,
        email,
        phone: phone || '',
        verificationStatus: 'DRAFT',
        submittedAt: createdAt
      });
    }

    await addDocHelper('notifications', {
      userId: uid,
      title: 'Welcome to MedPlus!',
      message: `Hello ${fullName}, your account has been successfully created.`,
      read: 0,
      type: 'SYSTEM',
      createdAt
    });

    res.status(201).json({ message: 'User registered successfully', uid });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(400).json({ error: error.message || 'Registration failed.' });
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }

  try {
    const authResult = await signInWithEmailAndPassword(auth, email, password);
    const uid = authResult.user.uid;

    const user = await getDocHelper('users', uid);
    if (!user) {
      return res.status(404).json({ error: 'User profile not found in database.' });
    }

    const token = jwt.sign(
      { uid: user.uid, email: user.email, role: user.role },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.json({
      token,
      uid: user.uid,
      email: user.email,
      role: user.role,
      fullName: user.fullName,
      phone: user.phone || '',
      profileImage: user.profileImage || ''
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(401).json({ error: 'Invalid email or password' });
  }
});

app.post('/api/auth/google', async (req, res) => {
  const { idToken, email, fullName, role } = req.body;
  if (!idToken) {
    return res.status(400).json({ error: 'ID Token is required' });
  }

  const finalEmail = email || `google_user_${idToken.substring(0, 8)}@gmail.com`;
  const finalName = fullName || `Google User ${idToken.substring(0, 4)}`;
  const finalRole = (role || 'PATIENT').toUpperCase();

  try {
    const users = await queryCollectionHelper('users', [{ field: 'email', operator: '==', value: finalEmail }]);
    let user;

    if (users.length > 0) {
      user = users[0];
    } else {
      const uid = `google_${crypto.randomUUID()}`;
      const createdAt = new Date().toISOString();

      user = {
        uid,
        fullName: finalName,
        email: finalEmail,
        phone: '',
        role: finalRole,
        profileImage: '',
        status: 'ACTIVE',
        createdAt
      };

      await setDocHelper('users', uid, user);

      if (finalRole === 'DOCTOR') {
        await setDocHelper('doctor_profiles', uid, {
          uid,
          fullName: finalName,
          email: finalEmail,
          phone: '',
          verificationStatus: 'DRAFT',
          submittedAt: createdAt
        });
      }

      await addDocHelper('notifications', {
        userId: uid,
        title: 'Welcome to MedPlus via Google!',
        message: `Hello ${finalName}, your account has been successfully created via Google Sign-In.`,
        read: 0,
        type: 'SYSTEM',
        createdAt
      });
    }

    const token = jwt.sign(
      { uid: user.uid, email: user.email, role: user.role },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.json({
      token,
      uid: user.uid,
      email: user.email,
      role: user.role,
      fullName: user.fullName,
      phone: user.phone || '',
      profileImage: user.profileImage || ''
    });
  } catch (error) {
    console.error('Google login error:', error);
    res.status(500).json({ error: 'Internal server error during Google auth' });
  }
});

app.get('/api/auth/profile/:uid', async (req, res) => {
  try {
    const user = await getDocHelper('users', req.params.uid);
    if (!user) {
      return res.status(404).json({ error: 'User profile not found' });
    }
    res.json(user);
  } catch (error) {
    console.error('Fetch profile error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/api/auth/profile/:uid', async (req, res) => {
  const { fullName, phone, profileImage } = req.body;
  try {
    const updates = {};
    if (fullName !== undefined) updates.fullName = fullName;
    if (phone !== undefined) updates.phone = phone;
    if (profileImage !== undefined) updates.profileImage = profileImage;

    await updateDocHelper('users', req.params.uid, updates);

    const docProfile = await getDocHelper('doctor_profiles', req.params.uid);
    if (docProfile) {
      await updateDocHelper('doctor_profiles', req.params.uid, updates);
    }

    res.json({ message: 'Profile updated successfully' });
  } catch (error) {
    console.error('Update profile error:', error);
    res.status(500).json({ error: 'Internal server error updating profile' });
  }
});

// ----------------------------------------------------
// DOCTORS ENDPOINTS
// ----------------------------------------------------

app.get('/api/doctors', async (req, res) => {
  try {
    const list1 = await queryCollectionHelper('doctor_profiles', [{ field: 'verificationStatus', operator: '==', value: 'VERIFIED' }]);
    const list2 = await queryCollectionHelper('doctor_profiles', [{ field: 'verificationStatus', operator: '==', value: 'APPROVED' }]);
    const list = [...list1, ...list2];

    const uniqueMap = {};
    list.forEach(d => {
      uniqueMap[d.uid] = d;
    });

    res.json(Object.values(uniqueMap));
  } catch (error) {
    console.error('Fetch verified doctors error:', error);
    res.status(500).json({ error: 'Internal server error fetching doctor list' });
  }
});

app.get('/api/doctors/:id', async (req, res) => {
  try {
    const docProfile = await getDocHelper('doctor_profiles', req.params.id);
    if (!docProfile) {
      return res.status(404).json({ error: 'Doctor profile not found' });
    }
    res.json(docProfile);
  } catch (error) {
    console.error('Get doctor profile error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.post('/api/doctors/:id/profile', async (req, res) => {
  try {
    const docProfile = await getDocHelper('doctor_profiles', req.params.id);

    // Filter out null or undefined fields from request body to prevent wiping out existing data
    const updates = {};
    for (const key in req.body) {
      if (req.body[key] !== null && req.body[key] !== undefined) {
        updates[key] = req.body[key];
      }
    }

    if (docProfile) {
      await updateDocHelper('doctor_profiles', req.params.id, updates);
    } else {
      await setDocHelper('doctor_profiles', req.params.id, { uid: req.params.id, ...updates });
    }

    // Create admin notification if submitting for verification
    if (req.body.verificationStatus === 'PENDING') {
      const docName = updates.fullName || (docProfile ? docProfile.fullName : 'Doctor');
      await addDocHelper('notifications', {
        userId: 'ADMIN',
        title: 'New Verification Request',
        message: `Dr. ${docName} has submitted documents for verification.`,
        read: 0,
        type: 'DOCTOR_VERIFICATION',
        doctorId: req.params.id,
        createdAt: new Date().toISOString()
      });
    }

    res.json({ message: 'Doctor profile saved successfully' });
  } catch (error) {
    console.error('Save doctor profile error:', error);
    res.status(500).json({ error: 'Internal server error saving profile' });
  }
});

app.get('/api/doctors/patients/:doctorId', async (req, res) => {
  const doctorId = req.params.doctorId;
  try {
    const appts = await queryCollectionHelper('appointments', [{ field: 'doctorId', operator: '==', value: doctorId }]);
    if (appts.length === 0) {
      return res.json([]);
    }

    const patientIds = [...new Set(appts.map(a => a.patientId))];
    const users = [];

    for (const uid of patientIds) {
      const user = await getDocHelper('users', uid);
      if (user) {
        users.push(user);
      }
    }

    res.json(users);
  } catch (error) {
    console.error('Fetch doctor patients error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ----------------------------------------------------
// APPOINTMENTS ENDPOINTS
// ----------------------------------------------------

app.post('/api/appointments/book', async (req, res) => {
  const { patientId, doctorId, doctorName, department, date, time, reason } = req.body;
  if (!patientId || !doctorId || !date || !time) {
    return res.status(400).json({ error: 'Missing appointment details' });
  }

  try {
    const patientProfile = await getDocHelper('users', patientId);
    const patientName = patientProfile ? patientProfile.fullName : 'Patient';

    const doctorProfile = await getDocHelper('doctor_profiles', doctorId);
    const startTimeStr = doctorProfile ? doctorProfile.consultationStartTime || '09:00' : '09:00';
    const slotDuration = doctorProfile ? doctorProfile.slotDuration || 15 : 15;

    const existing = (await queryCollectionHelper('appointments', [
      { field: 'doctorId', operator: '==', value: doctorId },
      { field: 'date', operator: '==', value: date }
    ])).filter(app => app.time === time && app.status !== 'CANCELLED');

    const diffMinutes = timeToMinutes(time) - timeToMinutes(startTimeStr);
    const baseToken = Math.max(1, Math.floor(diffMinutes / slotDuration));
    const tokenNumber = (baseToken + existing.length).toString();

    const appointmentId = crypto.randomUUID();
    const createdAt = new Date().toISOString();

    const appointmentData = {
      id: appointmentId,
      patientId,
      patientName,
      doctorId,
      doctorName: doctorName || 'Doctor',
      department: department || 'General',
      date,
      time,
      reason: reason || '',
      tokenNumber,
      status: 'UPCOMING',
      appointmentTimestamp: Date.now(),
      createdAt
    };

    await setDocHelper('appointments', appointmentId, appointmentData);

    const queueId = crypto.randomUUID();
    await setDocHelper('queue', queueId, {
      id: queueId,
      appointmentId,
      doctorId,
      patientId,
      patientName,
      tokenNumber,
      status: 'WAITING',
      department: department || 'General',
      date,
      isActive: true,
      estimatedWaitMinutes: 0,
      createdAt
    });

    await addDocHelper('notifications', {
      userId: patientId,
      title: 'Appointment Booked!',
      message: `Your slot is confirmed with ${doctorName} at ${time}. Token: #${tokenNumber}`,
      read: 0,
      type: 'APPOINTMENT',
      createdAt
    });

    await addDocHelper('notifications', {
      userId: doctorId,
      title: 'New Appointment',
      message: `Patient ${patientName} booked a slot at ${time}. Token: #${tokenNumber}`,
      read: 0,
      type: 'APPOINTMENT',
      createdAt
    });

    res.status(201).json(appointmentData);
  } catch (error) {
    console.error('Book appointment error:', error);
    res.status(500).json({ error: 'Internal server error booking appointment' });
  }
});

app.get('/api/appointments/patient/:patientId', async (req, res) => {
  try {
    const list = await queryCollectionHelper('appointments', [{ field: 'patientId', operator: '==', value: req.params.patientId }]);
    list.sort((a, b) => {
      const dateA = a.createdAt || '';
      const dateB = b.createdAt || '';
      return dateB.localeCompare(dateA);
    });
    res.json(list);
  } catch (error) {
    console.error('Fetch patient appointments error:', error);
    res.status(500).json({ error: 'Internal server error fetching patient appointments' });
  }
});

app.get('/api/appointments/doctor/:doctorId', async (req, res) => {
  try {
    const list = await queryCollectionHelper('appointments', [{ field: 'doctorId', operator: '==', value: req.params.doctorId }]);
    list.sort((a, b) => {
      const dateA = a.createdAt || '';
      const dateB = b.createdAt || '';
      return dateB.localeCompare(dateA);
    });
    res.json(list);
  } catch (error) {
    console.error('Fetch doctor appointments error:', error);
    res.status(500).json({ error: 'Internal server error fetching doctor appointments' });
  }
});

app.get('/api/appointments/:id', async (req, res) => {
  try {
    const appt = await getDocHelper('appointments', req.params.id);
    if (!appt) {
      return res.status(404).json({ error: 'Appointment not found' });
    }
    res.json(appt);
  } catch (error) {
    console.error('Get appointment details error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/api/appointments/:id/status', async (req, res) => {
  const { status } = req.body;
  if (!status) {
    return res.status(400).json({ error: 'Status is required' });
  }

  try {
    const appt = await getDocHelper('appointments', req.params.id);
    if (!appt) {
      return res.status(404).json({ error: 'Appointment not found' });
    }

    await updateDocHelper('appointments', req.params.id, { status: status.toUpperCase() });

    const queueStatus = status.toUpperCase() === 'COMPLETED' ? 'COMPLETED' : (status.toUpperCase() === 'CANCELLED' ? 'CANCELLED' : 'WAITING');
    const isActive = queueStatus === 'WAITING';

    const queueItems = await queryCollectionHelper('queue', [{ field: 'appointmentId', operator: '==', value: req.params.id }]);
    for (const item of queueItems) {
      await updateDocHelper('queue', item.id, { status: queueStatus, isActive });
    }

    await addDocHelper('notifications', {
      userId: appt.patientId,
      title: 'Appointment Updated',
      message: `Your appointment status with ${appt.doctorName} has been marked as ${status.toUpperCase()}.`,
      read: 0,
      type: 'APPOINTMENT',
      createdAt: new Date().toISOString()
    });

    res.json({ message: 'Appointment status updated successfully' });
  } catch (error) {
    console.error('Update appointment status error:', error);
    res.status(500).json({ error: 'Internal server error updating appointment status' });
  }
});

app.post('/api/appointments/reschedule', async (req, res) => {
  const { appointmentId, doctorId, doctorName, department, date, time, reason } = req.body;
  if (!appointmentId || !doctorId || !date || !time) {
    return res.status(400).json({ error: 'Missing reschedule details' });
  }

  try {
    const appt = await getDocHelper('appointments', appointmentId);
    if (!appt) {
      return res.status(404).json({ error: 'Appointment not found' });
    }

    const patientId = appt.patientId;
    const patientName = appt.patientName;

    const doctorProfile = await getDocHelper('doctor_profiles', doctorId);
    const startTimeStr = doctorProfile ? doctorProfile.consultationStartTime || '09:00' : '09:00';
    const slotDuration = doctorProfile ? doctorProfile.slotDuration || 15 : 15;

    const existing = (await queryCollectionHelper('appointments', [
      { field: 'doctorId', operator: '==', value: doctorId },
      { field: 'date', operator: '==', value: date }
    ])).filter(app => app.time === time && app.status !== 'CANCELLED');

    const diffMinutes = timeToMinutes(time) - timeToMinutes(startTimeStr);
    const baseToken = Math.max(1, Math.floor(diffMinutes / slotDuration));
    const tokenNumber = (baseToken + existing.length).toString();

    await updateDocHelper('appointments', appointmentId, {
      doctorId,
      doctorName: doctorName || 'Doctor',
      department: department || 'General',
      date,
      time,
      tokenNumber,
      status: 'UPCOMING'
    });

    const queueItems = await queryCollectionHelper('queue', [{ field: 'appointmentId', operator: '==', value: appointmentId }]);
    if (queueItems.length > 0) {
      for (const item of queueItems) {
        await updateDocHelper('queue', item.id, {
          date,
          tokenNumber,
          status: 'WAITING',
          isActive: true
        });
      }
    } else {
      const queueId = crypto.randomUUID();
      await setDocHelper('queue', queueId, {
        id: queueId,
        appointmentId,
        doctorId,
        patientId,
        patientName,
        tokenNumber,
        status: 'WAITING',
        department: department || 'General',
        date,
        isActive: true
      });
    }

    const createdAt = new Date().toISOString();

    await addDocHelper('notifications', {
      userId: patientId,
      title: 'Appointment Rescheduled',
      message: `Your appointment with Dr. ${doctorName} has been rescheduled to ${date} at ${time}. Token: #${tokenNumber}`,
      read: 0,
      type: 'APPOINTMENT',
      createdAt
    });

    await addDocHelper('notifications', {
      userId: doctorId,
      title: 'Appointment Rescheduled',
      message: `Patient ${patientName} rescheduled their appointment to ${date} at ${time}.`,
      read: 0,
      type: 'APPOINTMENT',
      createdAt
    });

    const updatedAppt = await getDocHelper('appointments', appointmentId);
    res.json(updatedAppt);
  } catch (error) {
    console.error('Reschedule appointment error:', error);
    res.status(500).json({ error: 'Internal server error rescheduling appointment' });
  }
});

// ----------------------------------------------------
// QUEUE OPERATIONS ENDPOINTS
// ----------------------------------------------------

app.get('/api/queue', async (req, res) => {
  const { doctorId, date } = req.query;
  if (!doctorId) {
    return res.status(400).json({ error: 'doctorId query parameter is required' });
  }

  try {
    const filters = [{ field: 'doctorId', operator: '==', value: doctorId }];
    if (date) {
      filters.push({ field: 'date', operator: '==', value: date });
    }
    const list = await queryCollectionHelper('queue', filters);

    list.forEach(item => {
      item.isActive = item.isActive === true;
      item.queueId = item.id;
      item._id = item.id;
    });

    res.json(list);
  } catch (error) {
    console.error('Fetch queue error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/api/queue/:id/status', async (req, res) => {
  const { status } = req.body;
  if (!status) {
    return res.status(400).json({ error: 'Status is required' });
  }

  try {
    const queueItem = await getDocHelper('queue', req.params.id);
    if (!queueItem) {
      return res.status(404).json({ error: 'Queue item not found' });
    }

    const newStatus = status.toUpperCase();
    const isActive = !(newStatus === 'COMPLETED' || newStatus === 'CANCELLED');

    await updateDocHelper('queue', req.params.id, { status: newStatus, isActive });

    let apptStatus = 'UPCOMING';
    if (newStatus === 'SERVING') apptStatus = 'ACTIVE';
    else if (newStatus === 'COMPLETED') apptStatus = 'COMPLETED';
    else if (newStatus === 'CANCELLED') apptStatus = 'CANCELLED';

    await updateDocHelper('appointments', queueItem.appointmentId, { status: apptStatus });

    const updated = await getDocHelper('queue', req.params.id);
    updated.isActive = updated.isActive === true;
    updated.queueId = updated.id;
    updated._id = updated.id;

    res.json(updated);
  } catch (error) {
    console.error('Update queue status error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ----------------------------------------------------
// MEDICAL RECORDS ENDPOINTS
// ----------------------------------------------------

app.post('/api/medical-records', async (req, res) => {
  const { appointmentId, patientId, diagnosis, prescription, notes, followUpDate, fileUrl } = req.body;
  if (!patientId || !appointmentId) {
    return res.status(400).json({ error: 'patientId and appointmentId are required' });
  }

  try {
    const appt = await getDocHelper('appointments', appointmentId);
    if (!appt) {
      return res.status(404).json({ error: 'Appointment not found' });
    }

    const id = crypto.randomUUID();
    const createdAt = new Date().toISOString();

    const recordData = {
      id,
      patientId,
      patientName: appt.patientName,
      doctorId: appt.doctorId,
      doctorName: appt.doctorName,
      appointmentId,
      diagnosis: diagnosis || '',
      prescription: prescription || '',
      notes: notes || '',
      followUpDate: followUpDate || '',
      fileUrl: fileUrl || '',
      createdAt
    };

    await setDocHelper('medical_records', id, recordData);

    await addDocHelper('notifications', {
      userId: patientId,
      title: 'New Medical Record Available',
      message: `Dr. ${appt.doctorName} has added a medical record for your consultation.`,
      read: 0,
      type: 'MEDICAL_RECORD',
      createdAt
    });

    res.status(201).json(recordData);
  } catch (error) {
    console.error('Create medical record error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/medical-records/patient/:patientId', async (req, res) => {
  try {
    const list = await queryCollectionHelper('medical_records', [{ field: 'patientId', operator: '==', value: req.params.patientId }]);
    list.sort((a, b) => {
      const dateA = a.createdAt || '';
      const dateB = b.createdAt || '';
      return dateB.localeCompare(dateA);
    });
    res.json(list);
  } catch (error) {
    console.error('Fetch medical records error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/medical-records/:id', async (req, res) => {
  try {
    const record = await getDocHelper('medical_records', req.params.id);
    if (!record) return res.status(404).json({ error: 'Medical record not found' });
    res.json(record);
  } catch (error) {
    console.error('Get medical record by id error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ----------------------------------------------------
// FEEDBACK ENDPOINTS
// ----------------------------------------------------

app.post('/api/feedback', async (req, res) => {
  const { doctorId, patientId, rating, feedback, appointmentId } = req.body;
  if (!doctorId || !patientId || !rating) {
    return res.status(400).json({ error: 'Missing feedback fields' });
  }

  try {
    const user = await getDocHelper('users', patientId);
    const patientName = user ? user.fullName : 'Patient';

    const id = crypto.randomUUID();
    const createdAt = new Date().toISOString();

    const fbData = {
      id,
      doctorId,
      patientId,
      patientName,
      appointmentId: appointmentId || null,
      rating,
      comment: feedback || '',
      createdAt
    };

    await setDocHelper('feedback', id, fbData);

    res.status(201).json({ ...fbData, feedback: fbData.comment });
  } catch (error) {
    console.error('Submit feedback error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/feedback/doctor/:doctorId', async (req, res) => {
  try {
    const list = await queryCollectionHelper('feedback', [{ field: 'doctorId', operator: '==', value: req.params.doctorId }]);
    list.sort((a, b) => {
      const dateA = a.createdAt || '';
      const dateB = b.createdAt || '';
      return dateB.localeCompare(dateA);
    });
    list.forEach(r => {
      r.feedback = r.comment;
    });
    res.json(list);
  } catch (error) {
    console.error('Fetch doctor reviews error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/feedback/appointment/:appointmentId', async (req, res) => {
  try {
    const list = await queryCollectionHelper('feedback', [{ field: 'appointmentId', operator: '==', value: req.params.appointmentId }]);
    if (list.length === 0) {
      return res.json({ exists: false });
    }
    const feedback = list[0];
    feedback.feedback = feedback.comment;
    res.json({ exists: true, feedback });
  } catch (error) {
    console.error('Fetch appointment feedback error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ----------------------------------------------------
// DASHBOARD STATS ENDPOINTS
// ----------------------------------------------------

app.get('/api/dashboard/patient/:patientId', async (req, res) => {
  const patientId = req.params.patientId;
  try {
    const appts = await queryCollectionHelper('appointments', [{ field: 'patientId', operator: '==', value: patientId }]);
    const records = await queryCollectionHelper('medical_records', [{ field: 'patientId', operator: '==', value: patientId }]);

    const completed = appts.filter(a => a.status === 'COMPLETED').length;
    const upcoming = appts.filter(a => a.status === 'PENDING' || a.status === 'UPCOMING' || a.status === 'ACTIVE')
      .sort((a, b) => (a.date + ' ' + a.time).localeCompare(b.date + ' ' + b.time))[0] || null;

    res.json({
      totalAppointments: appts.length,
      completedAppointments: completed,
      totalMedicalRecords: records.length,
      upcomingAppointment: upcoming
    });
  } catch (error) {
    console.error('Fetch patient dashboard stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/dashboard/doctor/:doctorId', async (req, res) => {
  const doctorId = req.params.doctorId;
  try {
    const appts = await queryCollectionHelper('appointments', [{ field: 'doctorId', operator: '==', value: doctorId }]);
    const reviews = await queryCollectionHelper('feedback', [{ field: 'doctorId', operator: '==', value: doctorId }]);

    const pending = appts.filter(a => a.status === 'PENDING' || a.status === 'UPCOMING').length;
    const completed = appts.filter(a => a.status === 'COMPLETED').length;

    const totalRating = reviews.reduce((acc, curr) => acc + (curr.rating || 0), 0);
    const avgRating = reviews.length > 0 ? parseFloat((totalRating / reviews.length).toFixed(1)) : 5.0;

    res.json({
      totalAppointments: appts.length,
      pendingAppointments: pending,
      completedAppointments: completed,
      averageRating: avgRating
    });
  } catch (error) {
    console.error('Fetch doctor dashboard stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/dashboard/admin', async (req, res) => {
  try {
    const users = await queryCollectionHelper('users');
    const docs = await queryCollectionHelper('doctor_profiles');
    const appts = await queryCollectionHelper('appointments');

    const patientsCount = users.filter(u => u.role === 'PATIENT').length;
    const pendingVerifications = docs.filter(d => d.verificationStatus === 'PENDING').length;

    res.json({
      totalPatients: patientsCount,
      totalDoctors: docs.length,
      totalAppointments: appts.length,
      pendingVerifications
    });
  } catch (error) {
    console.error('Fetch admin dashboard stats error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ----------------------------------------------------
// ADMIN ENDPOINTS
// ----------------------------------------------------

app.get('/api/admin/pending-doctors', async (req, res) => {
  try {
    const pending = await queryCollectionHelper('doctor_profiles', [{ field: 'verificationStatus', operator: '==', value: 'PENDING' }]);
    res.json(pending);
  } catch (error) {
    console.error('Fetch pending doctors error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/api/admin/verify-doctor/:doctorId', async (req, res) => {
  const doctorId = req.params.doctorId;
  const { status, rejectionReason, reviewedBy } = req.body;

  if (!status) {
    return res.status(400).json({ error: 'Status is required' });
  }

  try {
    await updateDocHelper('doctor_profiles', doctorId, {
      verificationStatus: status.toUpperCase(),
      rejectionReason: rejectionReason || null,
      reviewedBy: reviewedBy || 'ADMIN',
      reviewedAt: new Date().toISOString()
    });

    const title = status.toUpperCase() === 'APPROVED' || status.toUpperCase() === 'VERIFIED' ? 'Profile Approved!' : 'Profile Rejected';
    const message = status.toUpperCase() === 'APPROVED' || status.toUpperCase() === 'VERIFIED'
      ? 'Congratulations, your professional profile has been approved. You are now active on MedPlus.'
      : `Your profile verification failed. Reason: ${rejectionReason || 'Unspecified documents'}`;

    await addDocHelper('notifications', {
      userId: doctorId,
      title,
      message,
      read: 0,
      type: 'SYSTEM',
      createdAt: new Date().toISOString()
    });

    res.json({ message: `Doctor profile status updated to ${status}` });
  } catch (error) {
    console.error('Verify doctor error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/admin/doctors', async (req, res) => {
  try {
    const list = await queryCollectionHelper('doctor_profiles');
    res.json(list);
  } catch (error) {
    console.error('Fetch all doctors error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/admin/patients', async (req, res) => {
  try {
    const list = await queryCollectionHelper('users', [{ field: 'role', operator: '==', value: 'PATIENT' }]);
    res.json(list);
  } catch (error) {
    console.error('Fetch all patients error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.get('/api/admin/appointments', async (req, res) => {
  try {
    const list = await queryCollectionHelper('appointments');
    list.sort((a, b) => {
      const dateA = a.createdAt || '';
      const dateB = b.createdAt || '';
      return dateB.localeCompare(dateA);
    });
    res.json(list);
  } catch (error) {
    console.error('Fetch all appointments error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// ----------------------------------------------------
// NOTIFICATIONS ENDPOINTS
// ----------------------------------------------------

app.get('/api/notifications/:userId', async (req, res) => {
  try {
    const list = await queryCollectionHelper('notifications', [{ field: 'userId', operator: '==', value: req.params.userId }]);
    list.sort((a, b) => {
      const dateA = a.createdAt || '';
      const dateB = b.createdAt || '';
      return dateB.localeCompare(dateA);
    });
    res.json(list);
  } catch (error) {
    console.error('Fetch notifications error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.put('/api/notifications/:id/read', async (req, res) => {
  try {
    await updateDocHelper('notifications', req.params.id, { read: 1 });
    res.json({ message: 'Notification marked as read' });
  } catch (error) {
    console.error('Mark notification read error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.post('/api/notifications', async (req, res) => {
  const { userId, title, message, type } = req.body;
  if (!userId || !title || !message) {
    return res.status(400).json({ error: 'Missing notification attributes' });
  }

  try {
    const id = crypto.randomUUID();
    const createdAt = new Date().toISOString();
    await setDocHelper('notifications', id, {
      id,
      userId,
      title,
      message,
      read: 0,
      type: type || 'SYSTEM',
      createdAt
    });

    res.status(201).json({ id, message: 'Notification sent successfully' });
  } catch (error) {
    console.error('Send notification error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

app.delete('/api/notifications/:id', async (req, res) => {
  try {
    await deleteDocHelper('notifications', req.params.id);
    res.json({ message: 'Notification deleted' });
  } catch (error) {
    console.error('Delete notification error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Start listening
app.listen(PORT, '0.0.0.0', () => {
  console.log(`MedPlus Backend running on http://127.0.0.1:${PORT}`);
});

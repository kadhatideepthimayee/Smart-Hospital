const admin = require('firebase-admin');
const { getFirestore: getAdminFirestore } = require('firebase-admin/firestore');
const { initializeApp } = require('firebase/app');
const { getAuth } = require('firebase/auth');
const { getFirestore } = require('firebase/firestore');
const fs = require('fs');
const path = require('path');
const os = require('os');

// Load API key dynamically from home directory secrets
let firebaseApiKey = '';
const homeSecretsPath = path.join(os.homedir(), '.medplus_secrets');
let serviceAccount = null;

if (fs.existsSync(homeSecretsPath)) {
  try {
    const content = fs.readFileSync(homeSecretsPath, 'utf-8');
    content.split(/\r?\n/).forEach(line => {
      const match = line.match(/^\s*([\w.-]+)\s*=\s*(.*)?\s*$/);
      if (match) {
        const key = match[1];
        let value = match[2] || '';
        if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
          value = value.slice(1, -1);
        }
        if (key === 'FIREBASE_API_KEY' || key === 'VITE_FIREBASE_API_KEY') {
          firebaseApiKey = value.trim();
        }
      }
    });
  } catch (e) {
    console.error('Error reading secrets from backend:', e);
  }
}

// Check for Service Account Key JSON file
const serviceAccountPath = path.join(os.homedir(), '.medplus_secrets_key.json');
if (fs.existsSync(serviceAccountPath)) {
  try {
    serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf-8'));
    console.log('Firebase Admin SDK initialized using Service Account Key.');
  } catch (e) {
    console.error('Failed to parse serviceAccountKey.json:', e);
  }
}

if (!firebaseApiKey) {
  firebaseApiKey = process.env.FIREBASE_API_KEY || '';
}

const firebaseConfig = {
  apiKey: firebaseApiKey,
  authDomain: 'medplus-a50ca.firebaseapp.com',
  projectId: 'medplus-a50ca',
  storageBucket: 'medplus-a50ca.firebasestorage.app',
  messagingSenderId: '366692040766',
  appId: '1:366692040766:web:2ea8d3a114a45de8e5516f'
};

// Initialize Client SDK
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// Initialize Admin SDK if key is present
let adminDb = null;
if (serviceAccount) {
  try {
    admin.initializeApp({
      credential: admin.cert(serviceAccount)
    });
    adminDb = getAdminFirestore();
  } catch (e) {
    console.error('Error initializing Firebase Admin SDK:', e);
  }
}

module.exports = {
  app,
  auth,
  db,
  adminDb,
  usingAdmin: !!adminDb
};

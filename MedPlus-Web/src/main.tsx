import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { AuthProvider } from './context/AuthContext';
import { isFirebaseConfigured } from './lib/firebase';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

const FirebaseWarning = () => (
  <div style={{
    fontFamily: 'Inter, system-ui, -apple-system, BlinkMacSystemFont, sans-serif',
    background: '#0B0F19',
    color: '#F3F4F6',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '100vh',
    padding: '20px',
    textAlign: 'center',
    boxSizing: 'border-box'
  }}>
    <div style={{
      maxWidth: '550px',
      background: '#1F2937',
      padding: '40px',
      borderRadius: '24px',
      boxShadow: '0 10px 30px rgba(0,0,0,0.4)',
      border: '1px solid #374151'
    }}>
      <div style={{ fontSize: '56px', marginBottom: '20px' }}>⚡</div>
      <h1 style={{ fontSize: '24px', fontWeight: '800', marginBottom: '16px', color: '#10B981', letterSpacing: '-0.025em' }}>
        Firebase Setup Required
      </h1>
      <p style={{ color: '#9CA3AF', fontSize: '15px', lineHeight: '1.6', marginBottom: '24px' }}>
        The MedPlus web app has been successfully migrated to Firebase. 
        Please configure your Firebase credentials in the web configuration file to launch the application.
      </p>
      
      <div style={{
        textAlign: 'left',
        background: '#111827',
        padding: '20px',
        borderRadius: '16px',
        fontSize: '13px',
        fontFamily: 'Consolas, Monaco, monospace',
        color: '#34D399',
        marginBottom: '24px',
        border: '1px solid #111827',
        overflowX: 'auto',
        lineHeight: '1.5'
      }}>
        <div style={{ color: '#6B7280', marginBottom: '8px' }}># Paste your credentials in MedPlus-Web/.env:</div>
        <div style={{ color: '#F3F4F6' }}>VITE_FIREBASE_API_KEY=your_api_key</div>
        <div style={{ color: '#F3F4F6' }}>VITE_FIREBASE_AUTH_DOMAIN=your_auth_domain</div>
        <div style={{ color: '#F3F4F6' }}>VITE_FIREBASE_PROJECT_ID=your_project_id</div>
        <div style={{ color: '#F3F4F6' }}>VITE_FIREBASE_STORAGE_BUCKET=your_storage_bucket</div>
        <div style={{ color: '#F3F4F6' }}>VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id</div>
        <div style={{ color: '#F3F4F6' }}>VITE_FIREBASE_APP_ID=your_app_id</div>
      </div>
      
      <p style={{ fontSize: '13px', color: '#9CA3AF' }}>
        After adding your credentials, please **restart** your Vite web development server and refresh this page.
      </p>
    </div>
  </div>
);

if (!isFirebaseConfigured) {
  ReactDOM.createRoot(document.getElementById('root')!).render(<FirebaseWarning />);
} else {
  ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </React.StrictMode>
  );
}

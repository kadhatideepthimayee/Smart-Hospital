import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import Layout from './components/Layout';
import Auth from './pages/Auth';

// Patient pages
import PatientDashboard from './pages/patient/PatientDashboard';
import BookAppointment from './pages/patient/BookAppointment';
import MyAppointments from './pages/patient/MyAppointments';
import QueueTracking from './pages/patient/QueueTracking';
import PatientMedicalRecords from './pages/patient/PatientMedicalRecords';
import PatientNotifications from './pages/patient/PatientNotifications';
import PatientProfile from './pages/patient/PatientProfile';

// Doctor pages
import DoctorDashboard from './pages/doctor/DoctorDashboard';
import DoctorAppointments from './pages/doctor/DoctorAppointments';
import DoctorQueue from './pages/doctor/DoctorQueue';
import DoctorPatients from './pages/doctor/DoctorPatients';
import DoctorAvailability from './pages/doctor/DoctorAvailability';
import DoctorNotifications from './pages/doctor/DoctorNotifications';
import DoctorProfile from './pages/doctor/DoctorProfile';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import DoctorVerification from './pages/admin/DoctorVerification';
import AdminDoctors from './pages/admin/AdminDoctors';
import AdminPatients from './pages/admin/AdminPatients';
import AdminAppointments from './pages/admin/AdminAppointments';
import AdminNotifications from './pages/admin/AdminNotifications';
import AdminProfile from './pages/admin/AdminProfile';

const App: React.FC = () => {
  const { user } = useAuth();

  return (
    <Routes>
      <Route 
        path="/login" 
        element={
          !user ? (
            <Auth />
          ) : user.role === 'PATIENT' ? (
            <Navigate to="/patient/dashboard" replace />
          ) : user.role === 'DOCTOR' ? (
            <Navigate to="/doctor/dashboard" replace />
          ) : (
            <Navigate to="/admin/dashboard" replace />
          )
        } 
      />

      {/* Root redirection */}
      <Route 
        path="/" 
        element={
          user ? (
            user.role === 'PATIENT' ? (
              <Navigate to="/patient/dashboard" replace />
            ) : user.role === 'DOCTOR' ? (
              <Navigate to="/doctor/dashboard" replace />
            ) : (
              <Navigate to="/admin/dashboard" replace />
            )
          ) : (
            <Navigate to="/login" replace />
          )
        } 
      />

      {/* Patient Portal Routes */}
      <Route 
        path="/patient" 
        element={
          <ProtectedRoute allowedRoles={['PATIENT']}>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<PatientDashboard />} />
        <Route path="book" element={<BookAppointment />} />
        <Route path="appointments" element={<MyAppointments />} />
        <Route path="queue" element={<QueueTracking />} />
        <Route path="records" element={<PatientMedicalRecords />} />
        <Route path="notifications" element={<PatientNotifications />} />
        <Route path="profile" element={<PatientProfile />} />
        <Route path="*" element={<Navigate to="dashboard" replace />} />
      </Route>

      {/* Doctor Portal Routes */}
      <Route 
        path="/doctor" 
        element={
          <ProtectedRoute allowedRoles={['DOCTOR']}>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<DoctorDashboard />} />
        <Route path="appointments" element={<DoctorAppointments />} />
        <Route path="queue" element={<DoctorQueue />} />
        <Route path="patients" element={<DoctorPatients />} />
        <Route path="availability" element={<DoctorAvailability />} />
        <Route path="notifications" element={<DoctorNotifications />} />
        <Route path="profile" element={<DoctorProfile />} />
        <Route path="*" element={<Navigate to="dashboard" replace />} />
      </Route>

      {/* Admin Portal Routes */}
      <Route 
        path="/admin" 
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="verification" element={<DoctorVerification />} />
        <Route path="doctors" element={<AdminDoctors />} />
        <Route path="patients" element={<AdminPatients />} />
        <Route path="appointments" element={<AdminAppointments />} />
        <Route path="notifications" element={<AdminNotifications />} />
        <Route path="profile" element={<AdminProfile />} />
        <Route path="*" element={<Navigate to="dashboard" replace />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;

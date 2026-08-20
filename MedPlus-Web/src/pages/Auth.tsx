import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { forgotPassword, resetPassword } from '../api/auth';
import { 
  Mail, 
  Lock, 
  Eye, 
  EyeOff, 
  User as UserIcon, 
  Phone, 
  ArrowLeft, 
  CheckCircle, 
  Stethoscope, 
  UserCheck,
  Activity,
  Shield,
  AlertCircle,
  Clock,
  ChevronRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const Auth: React.FC = () => {
  const { login, register, googleSignIn } = useAuth();
  
  // Navigation states: 'login' | 'role_select' | 'register' | 'forgot'
  const [tab, setTab] = useState<'login' | 'role_select' | 'register' | 'forgot'>('login');
  
  // Registration Role: 'PATIENT' | 'DOCTOR'
  const [selectedRole, setSelectedRole] = useState<'PATIENT' | 'DOCTOR'>('PATIENT');

  // Input states
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);

  // Field validation errors
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [fullNameError, setFullNameError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');

  // Password visibility
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Forgot Password / PIN verification state
  const [resetStep, setResetStep] = useState<1 | 2>(1); // 1: send email, 2: reset
  const [resetCode, setResetCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [debugPin, setDebugPin] = useState('');

  // Google Login role popup state
  const [showGoogleRoleModal, setShowGoogleRoleModal] = useState(false);
  const [selectedGoogleRole, setSelectedGoogleRole] = useState<'PATIENT' | 'DOCTOR'>('PATIENT');
  const selectedGoogleRoleRef = React.useRef<'PATIENT' | 'DOCTOR'>('PATIENT');



  // Global loading and toast alerts
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null);

  // Load Remembered email on startup
  useEffect(() => {
    const rememberedEmail = localStorage.getItem('remembered_email');
    if (rememberedEmail) {
      setEmail(rememberedEmail);
      setRememberMe(true);
    }
  }, []);

  const triggerToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 4000);
  };

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (/^\d*$/.test(val) && val.length <= 10) {
      setPhone(val);
      setPhoneError('');
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setEmailError('');
    setPasswordError('');

    let valid = true;
    if (!email.trim()) {
      setEmailError('Email address is required');
      valid = false;
    }
    if (!password) {
      setPasswordError('Password is required');
      valid = false;
    }

    if (!valid) return;

    setLoading(true);
    try {
      if (rememberMe) {
        localStorage.setItem('remembered_email', email.trim());
      } else {
        localStorage.removeItem('remembered_email');
      }

      await login(email.trim(), password);
    } catch (err: any) {
      const errMsg = err.response?.data?.msg || err.message || 'Authentication failed. Please verify credentials.';
      triggerToast(errMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    
    setFullNameError('');
    setEmailError('');
    setPhoneError('');
    setPasswordError('');
    setConfirmPasswordError('');

    let valid = true;
    if (!fullName.trim()) {
      setFullNameError('Full name is required');
      valid = false;
    }
    if (!email.trim()) {
      setEmailError('Email address is required');
      valid = false;
    }
    if (!phone.trim()) {
      setPhoneError('Phone number is required');
      valid = false;
    } else if (phone.length !== 10) {
      setPhoneError('Phone number must be exactly 10 digits');
      valid = false;
    }
    if (!password) {
      setPasswordError('Password is required');
      valid = false;
    } else if (password.length < 6) {
      setPasswordError('Password must be at least 6 characters');
      valid = false;
    }
    if (!confirmPassword) {
      setConfirmPasswordError('Confirm password is required');
      valid = false;
    } else if (confirmPassword !== password) {
      setConfirmPasswordError('Passwords do not match');
      valid = false;
    }

    if (!valid) return;

    setLoading(true);
    try {
      await register(fullName.trim(), email.trim(), phone, password, selectedRole);
    } catch (err: any) {
      const errMsg = err.response?.data?.msg || err.message || 'Registration failed';
      triggerToast(errMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleFirebaseGoogleSignIn = async () => {
    setShowGoogleRoleModal(false);
    setLoading(true);
    try {
      const role = selectedGoogleRoleRef.current;
      await googleSignIn(role);
      triggerToast('Google authentication successful', 'success');
    } catch (err: any) {
      const errMsg = err.message || 'Google authentication failed';
      triggerToast(errMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleRoleSelect = (role: 'PATIENT' | 'DOCTOR') => {
    setSelectedGoogleRole(role);
    selectedGoogleRoleRef.current = role;
  };

  const handleSendResetCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) {
      setEmailError('Email address is required');
      return;
    }
    setEmailError('');
    setLoading(true);
    try {
      const data = await forgotPassword(email.trim());
      setDebugPin(data.debugPin || '');
      triggerToast('Reset PIN sent successfully', 'success');
    } catch (err: any) {
      const errMsg = err.response?.data?.msg || err.message || 'Email address not found';
      triggerToast(errMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resetCode) {
      triggerToast('Please enter the 6-digit PIN code', 'error');
      return;
    }
    if (!newPassword) {
      triggerToast('Please enter your new password', 'error');
      return;
    }

    setLoading(true);
    try {
      await resetPassword(email.trim(), resetCode, newPassword);
      triggerToast('Password reset successfully. You can now login.', 'success');
      setTab('login');
      setResetStep(1);
      setResetCode('');
    } catch (err: any) {
      const errMsg = err.response?.data?.msg || err.message || 'Reset failed. Invalid/expired PIN.';
      triggerToast(errMsg, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-slate-50 relative overflow-hidden font-sans">
      
      {/* Toast Alert */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: -50, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -50, scale: 0.95 }}
            className={`fixed top-5 left-1/2 -translate-x-1/2 z-55 flex items-center gap-3 px-5 py-3.5 rounded-2xl shadow-xl text-sm font-semibold max-w-md border ${
              toast.type === 'success' ? 'bg-emerald-600 border-emerald-500 text-white shadow-emerald-500/10' :
              toast.type === 'error' ? 'bg-rose-600 border-rose-500 text-white shadow-rose-500/10' :
              'bg-slate-900 border-slate-800 text-white'
            }`}
          >
            <AlertCircle size={18} className="flex-shrink-0" />
            <span>{toast.message}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* LEFT SPLIT PANEL: CLINICAL PORTFOLIO GRAPHIC */}
      <div className="w-full md:w-[45%] bg-gradient-to-br from-slate-900 via-slate-950 to-medical-blue-950 p-8 md:p-16 flex flex-col justify-between text-white relative min-h-[400px] md:min-h-screen">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-medical-teal-900/20 via-transparent to-transparent pointer-events-none"></div>
        <div 
          className="absolute inset-0 bg-cover bg-center opacity-10 select-none pointer-events-none filter grayscale mix-blend-overlay" 
          style={{ backgroundImage: "url('https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80')" }}
        ></div>

        {/* Branding header */}
        <div className="flex items-center gap-3 z-10 select-none">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-medical-blue-600 to-medical-teal-500 flex items-center justify-center shadow-lg shadow-medical-blue-500/20">
            <svg className="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3.5} d="M19 10.5h-5.5V5h-3v5.5H5v3h5.5V19h3v-5.5H19v-3z" />
            </svg>
          </div>
          <span className="text-xl font-extrabold tracking-tight bg-gradient-to-r from-white to-slate-200 bg-clip-text text-transparent">MedPlus</span>
        </div>

        {/* Hero Copy */}
        <div className="z-10 mt-16 md:mt-24 max-w-md">
          <h2 className="text-3xl md:text-4xl font-extrabold leading-tight text-white tracking-tight">
            Smart Hospital<br/>
            <span className="bg-gradient-to-r from-medical-blue-400 to-medical-teal-400 bg-clip-text text-transparent">Queue Management.</span>
          </h2>
          <p className="mt-4 text-sm text-slate-400 leading-relaxed max-w-sm">
            Book appointments instantly, consult with verified medical specialists, track queue estimations in real-time, and access digital prescriptions securely.
          </p>
        </div>

        {/* Highlights grid */}
        <div className="grid grid-cols-2 gap-6 mt-16 md:mt-24 z-10">
          <div className="flex items-center gap-3 bg-white/5 border border-white/10 p-3.5 rounded-2xl backdrop-blur-xs">
            <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-medical-teal-400">
              <Activity size={20} />
            </div>
            <div>
              <h4 className="font-bold text-xs text-white">Live Queue</h4>
              <p className="text-[10px] text-slate-400 leading-normal">Real-time estimations</p>
            </div>
          </div>
          
          <div className="flex items-center gap-3 bg-white/5 border border-white/10 p-3.5 rounded-2xl backdrop-blur-xs">
            <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center text-medical-blue-400">
              <Shield size={20} />
            </div>
            <div>
              <h4 className="font-bold text-xs text-white">Verified Staff</h4>
              <p className="text-[10px] text-slate-400 leading-normal">Credentialed doctors</p>
            </div>
          </div>
        </div>

        {/* Footer info */}
        <div className="z-10 mt-16 md:mt-24 text-xs text-slate-500 font-medium">
          &copy; {new Date().getFullYear()} MedPlus Secure Healthcare Network.
        </div>
      </div>

      {/* RIGHT SPLIT PANEL: DYNAMIC AUTHORIZATION CARD */}
      <div className="w-full md:w-[55%] flex items-center justify-center p-6 md:p-12 lg:p-24 bg-slate-50">
        <div className="w-full max-w-md bg-white rounded-3xl border border-slate-100 p-8 shadow-[0_4px_24px_rgba(0,0,0,0.02)] relative">
          
          <AnimatePresence mode="wait">
            {/* LOGIN FORM */}
            {tab === 'login' && (
              <motion.div
                key="login"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.15 }}
              >
                <div>
                  <h3 className="text-2xl font-bold text-slate-900 tracking-tight">Welcome Back</h3>
                  <p className="text-slate-500 text-sm mt-1">Sign in to continue managing your healthcare.</p>
                </div>

                <form onSubmit={handleLogin} className="space-y-4 mt-6" autoComplete="off">
                  {/* Email */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Email Address</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <Mail size={16} />
                      </div>
                      <input
                        type="email"
                        autoComplete="new-username"
                        className={`w-full pl-11 pr-4 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          emailError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="patient@medplus.com"
                        value={email}
                        onChange={(e) => { setEmail(e.target.value); setEmailError(''); }}
                        disabled={loading}
                      />
                    </div>
                    {emailError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {emailError}
                      </p>
                    )}
                  </div>

                  {/* Password */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Password</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <Lock size={16} />
                      </div>
                      <input
                        type={showPassword ? 'text' : 'password'}
                        autoComplete="new-password"
                        className={`w-full pl-11 pr-12 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          passwordError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="••••••••"
                        value={password}
                        onChange={(e) => { setPassword(e.target.value); setPasswordError(''); }}
                        disabled={loading}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                      >
                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                    {passwordError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {passwordError}
                      </p>
                    )}
                  </div>

                  {/* Options */}
                  <div className="flex items-center justify-between text-sm py-1 select-none">
                    <label className="flex items-center gap-2 text-slate-600 font-medium cursor-pointer">
                      <input
                        type="checkbox"
                        className="rounded border-slate-300 text-medical-blue-600 focus:ring-medical-blue-500/20 w-4 h-4 cursor-pointer"
                        checked={rememberMe}
                        onChange={(e) => setRememberMe(e.target.checked)}
                        disabled={loading}
                      />
                      Remember Me
                    </label>
                    <button
                      type="button"
                      onClick={() => { if (!loading) { setTab('forgot'); setResetStep(1); } }}
                      className="text-xs font-bold text-medical-blue-600 hover:text-medical-blue-700 transition-colors"
                    >
                      Forgot Password?
                    </button>
                  </div>

                  {/* Submit */}
                  <button
                    type="submit"
                    className="w-full bg-gradient-to-r from-medical-blue-600 to-medical-teal-600 hover:from-medical-blue-700 hover:to-medical-teal-700 text-white font-bold py-3 px-4 rounded-xl shadow-md shadow-medical-blue-500/10 transition-all hover:-translate-y-[1px] active:translate-y-0 active:scale-98 disabled:opacity-50 disabled:cursor-not-allowed text-sm mt-2 flex items-center justify-center gap-2 cursor-pointer"
                    disabled={loading}
                  >
                    {loading ? (
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    ) : 'Sign In'}
                  </button>
                </form>

                {/* Google OAuth Button */}
                <div className="relative flex items-center justify-center my-6">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-slate-100"></div>
                  </div>
                  <span className="relative bg-white px-3 text-[10px] font-bold text-slate-400 uppercase tracking-wider">Or continue with</span>
                </div>

                <button
                  type="button"
                  onClick={() => setShowGoogleRoleModal(true)}
                  className="w-full border border-slate-200 hover:border-slate-300 hover:bg-slate-50 text-slate-700 font-bold py-2.5 px-4 rounded-xl flex items-center justify-center gap-2.5 transition-all text-sm cursor-pointer hover:-translate-y-[1px] active:translate-y-0"
                  disabled={loading}
                >
                  <svg className="w-4 h-4" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v3.92h6.69c-.29 1.5-.14 3.01-3 4l4.51 3.52c2.64-2.43 4.54-6 4.54-9.35z"/>
                    <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-4.51-3.52c-1.25.84-2.85 1.34-4.62 1.34-3.56 0-6.58-2.4-7.66-5.64H2.43v3.62C4.42 20.73 7.98 24 12 24z"/>
                    <path fill="#FBBC05" d="M4.34 13.27c-.27-.81-.42-1.68-.42-2.57 0-.89.15-1.76.42-2.57V4.51H2.43C1.56 6.27 1 8.27 1 10.7c0 2.43.56 4.43 1.43 6.19l3.62-3.62z"/>
                    <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.93 1.19 15.21 0 12 0 7.98 0 4.42 3.27 2.43 7.08l3.62 3.62c1.08-3.24 4.1-5.64 7.66-5.64z"/>
                  </svg>
                  <span>Google Account</span>
                </button>

                {/* Redirect to signup */}
                <div className="mt-8 text-center text-sm select-none">
                  <span className="text-slate-500 font-medium">New to MedPlus? </span>
                  <button
                    type="button"
                    onClick={() => { setTab('role_select'); }}
                    className="font-bold text-medical-blue-600 hover:text-medical-blue-700 transition-colors"
                  >
                    Create Account
                  </button>
                </div>
              </motion.div>
            )}

            {/* ROLE SELECT FOR SIGNUP */}
            {tab === 'role_select' && (
              <motion.div
                key="role_select"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.15 }}
              >
                <div>
                  <button
                    type="button"
                    onClick={() => setTab('login')}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-slate-800 mb-6 transition-colors bg-transparent border-none p-0 cursor-pointer"
                  >
                    <ArrowLeft size={14} /> Back to Sign In
                  </button>
                  <h3 className="text-2xl font-bold text-slate-900 tracking-tight">Select Account Role</h3>
                  <p className="text-slate-500 text-sm mt-1">Choose the type of account you want to register.</p>
                </div>

                <div className="space-y-4 mt-6">
                  {/* Patient role card */}
                  <div
                    onClick={() => { setSelectedRole('PATIENT'); setTab('register'); }}
                    className="flex items-center gap-4 p-5 rounded-2xl border-2 border-slate-100 hover:border-medical-blue-500 bg-white hover:bg-slate-50/50 cursor-pointer shadow-[0_2px_8px_rgba(0,0,0,0.01)] hover:shadow-md transition-all group"
                  >
                    <div className="w-12 h-12 rounded-xl bg-medical-blue-50 text-medical-blue-600 flex items-center justify-center group-hover:scale-105 transition-transform">
                      <UserCheck size={24} />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-slate-900 text-sm">Patient Portal</h4>
                      <p className="text-xs text-slate-400 mt-0.5">Book consultations & track waiting lines</p>
                    </div>
                    <ChevronRight size={16} className="text-slate-350 group-hover:text-slate-500 group-hover:translate-x-0.5 transition-all" />
                  </div>

                  {/* Doctor role card */}
                  <div
                    onClick={() => { setSelectedRole('DOCTOR'); setTab('register'); }}
                    className="flex items-center gap-4 p-5 rounded-2xl border-2 border-slate-100 hover:border-medical-teal-500 bg-white hover:bg-slate-50/50 cursor-pointer shadow-[0_2px_8px_rgba(0,0,0,0.01)] hover:shadow-md transition-all group"
                  >
                    <div className="w-12 h-12 rounded-xl bg-medical-teal-50 text-medical-teal-600 flex items-center justify-center group-hover:scale-105 transition-transform">
                      <Stethoscope size={24} />
                    </div>
                    <div className="flex-1">
                      <h4 className="font-bold text-slate-900 text-sm">Doctor Console</h4>
                      <p className="text-xs text-slate-400 mt-0.5">Manage schedules, queues, & medical logs</p>
                    </div>
                    <ChevronRight size={16} className="text-slate-350 group-hover:text-slate-500 group-hover:translate-x-0.5 transition-all" />
                  </div>
                </div>
              </motion.div>
            )}

            {/* REGISTER FORM */}
            {tab === 'register' && (
              <motion.div
                key="register"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.15 }}
              >
                <div>
                  <button
                    type="button"
                    onClick={() => setTab('role_select')}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-slate-800 mb-6 transition-colors bg-transparent border-none p-0 cursor-pointer"
                  >
                    <ArrowLeft size={14} /> Back to Role Selection
                  </button>
                  <h3 className="text-2xl font-bold text-slate-900 tracking-tight">Create {selectedRole === 'DOCTOR' ? 'Doctor' : 'Patient'} Account</h3>
                  <p className="text-slate-500 text-sm mt-1">Complete your registration credentials below.</p>
                </div>

                <form onSubmit={handleRegister} className="space-y-4 mt-6 max-h-[60vh] overflow-y-auto pr-1 scrollbar-thin">
                  {/* Full name */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Full Name</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <UserIcon size={16} />
                      </div>
                      <input
                        type="text"
                        className={`w-full pl-11 pr-4 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          fullNameError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="John Doe"
                        value={fullName}
                        onChange={(e) => { setFullName(e.target.value); setFullNameError(''); }}
                        disabled={loading}
                      />
                    </div>
                    {fullNameError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {fullNameError}
                      </p>
                    )}
                  </div>

                  {/* Email */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Email Address</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <Mail size={16} />
                      </div>
                      <input
                        type="email"
                        className={`w-full pl-11 pr-4 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          emailError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="john@example.com"
                        value={email}
                        onChange={(e) => { setEmail(e.target.value); setEmailError(''); }}
                        disabled={loading}
                      />
                    </div>
                    {emailError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {emailError}
                      </p>
                    )}
                  </div>

                  {/* Phone */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Phone Number</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <Phone size={16} />
                      </div>
                      <input
                        type="tel"
                        className={`w-full pl-11 pr-4 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          phoneError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="10-digit number"
                        value={phone}
                        onChange={handlePhoneChange}
                        disabled={loading}
                      />
                    </div>
                    {phoneError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {phoneError}
                      </p>
                    )}
                  </div>

                  {/* Password */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Password</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <Lock size={16} />
                      </div>
                      <input
                        type={showPassword ? 'text' : 'password'}
                        className={`w-full pl-11 pr-12 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          passwordError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="At least 6 characters"
                        value={password}
                        onChange={(e) => { setPassword(e.target.value); setPasswordError(''); }}
                        disabled={loading}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                      >
                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                    {passwordError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {passwordError}
                      </p>
                    )}
                  </div>

                  {/* Confirm Password */}
                  <div>
                    <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Confirm Password</label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                        <Lock size={16} />
                      </div>
                      <input
                        type={showConfirmPassword ? 'text' : 'password'}
                        className={`w-full pl-11 pr-12 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none ${
                          confirmPasswordError ? 'border-rose-500' : 'border-slate-200 hover:border-slate-350/85'
                        }`}
                        placeholder="Re-enter password"
                        value={confirmPassword}
                        onChange={(e) => { setConfirmPassword(e.target.value); setConfirmPasswordError(''); }}
                        disabled={loading}
                      />
                      <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                      >
                        {showConfirmPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                      </button>
                    </div>
                    {confirmPasswordError && (
                      <p className="text-xs text-rose-600 font-semibold mt-1 flex items-center gap-1">
                        <AlertCircle size={12} />
                        {confirmPasswordError}
                      </p>
                    )}
                  </div>

                  {/* Register Button */}
                  <button
                    type="submit"
                    className="w-full bg-gradient-to-r from-medical-blue-600 to-medical-teal-600 hover:from-medical-blue-700 hover:to-medical-teal-700 text-white font-bold py-3 px-4 rounded-xl shadow-md shadow-medical-blue-500/10 transition-all hover:-translate-y-[1px] active:translate-y-0 active:scale-98 disabled:opacity-50 disabled:cursor-not-allowed text-sm mt-4 flex items-center justify-center gap-2 cursor-pointer"
                    disabled={loading}
                  >
                    {loading ? (
                      <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    ) : 'Register Account'}
                  </button>
                </form>
              </motion.div>
            )}

            {/* FORGOT PASSWORD FLOW */}
            {tab === 'forgot' && (
              <motion.div
                key="forgot"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.15 }}
              >
                <div>
                  <button
                    type="button"
                    onClick={() => { setTab('login'); setResetStep(1); }}
                    className="inline-flex items-center gap-1 text-xs font-semibold text-slate-500 hover:text-slate-800 mb-6 transition-colors bg-transparent border-none p-0 cursor-pointer"
                  >
                    <ArrowLeft size={14} /> Back to Sign In
                  </button>
                  <h3 className="text-2xl font-bold text-slate-900 tracking-tight">Recover Password</h3>
                  <p className="text-slate-500 text-sm mt-1">
                    {resetStep === 1 
                      ? 'Enter your email address to generate a 6-digit numeric reset PIN.' 
                      : 'Verify the reset code and enter your new password.'
                    }
                  </p>
                </div>

                {resetStep === 1 ? (
                  /* Step 1: Send reset PIN */
                  <form onSubmit={handleSendResetCode} className="space-y-4 mt-6">
                    <div>
                      <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">Email Address</label>
                      <div className="relative">
                        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                          <Mail size={16} />
                        </div>
                        <input
                          type="email"
                          className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none"
                          placeholder="registered@medplus.com"
                          value={email}
                          onChange={(e) => { setEmail(e.target.value); setEmailError(''); }}
                          disabled={loading}
                        />
                      </div>
                    </div>

                    <button
                      type="submit"
                      className="w-full bg-gradient-to-r from-medical-blue-600 to-medical-teal-600 hover:from-medical-blue-700 hover:to-medical-teal-700 text-white font-bold py-3 px-4 rounded-xl shadow-md shadow-medical-blue-500/10 transition-all hover:-translate-y-[1px] active:translate-y-0 active:scale-98 disabled:opacity-50 disabled:cursor-not-allowed text-sm mt-4 flex items-center justify-center gap-2 cursor-pointer"
                      disabled={loading}
                    >
                      {loading ? (
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      ) : 'Generate Reset PIN'}
                    </button>
                  </form>
                ) : (
                  /* Step 2: Input code and reset */
                  <form onSubmit={handleResetPassword} className="space-y-4 mt-6">
                    
                    {/* Sandbox Alert containing the debug PIN */}
                    {debugPin && (
                      <div className="p-4 bg-amber-50/70 border border-amber-250 rounded-2xl flex gap-3.5 text-xs text-amber-800 leading-relaxed font-medium shadow-xs">
                        <Clock size={20} className="text-amber-600 flex-shrink-0" />
                        <div>
                          <p className="font-extrabold text-amber-900 mb-0.5">Offline Sandbox PIN</p>
                          <p>
                            We bypassed email SMTP requirements. For offline development, use test code: 
                            <strong className="text-sm font-black text-slate-900 ml-1.5 select-all">{debugPin}</strong>
                          </p>
                        </div>
                      </div>
                    )}

                    {/* Reset Code */}
                    <div>
                      <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">6-Digit Reset PIN</label>
                      <input
                        type="text"
                        maxLength={6}
                        className="w-full text-center tracking-widest text-lg font-black py-2.5 rounded-xl border border-slate-200 focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none"
                        placeholder="000000"
                        value={resetCode}
                        onChange={(e) => setResetCode(e.target.value.replace(/\D/g, ''))}
                        disabled={loading}
                      />
                    </div>

                    {/* New Password */}
                    <div>
                      <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">New Password</label>
                      <div className="relative">
                        <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                          <Lock size={16} />
                        </div>
                        <input
                          type={showPassword ? 'text' : 'password'}
                          className="w-full pl-11 pr-12 py-2.5 rounded-xl border border-slate-200 text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none"
                          placeholder="At least 6 characters"
                          value={newPassword}
                          onChange={(e) => setNewPassword(e.target.value)}
                          disabled={loading}
                        />
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                        >
                          {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                        </button>
                      </div>
                    </div>

                    <button
                      type="submit"
                      className="w-full bg-gradient-to-r from-medical-blue-600 to-medical-teal-600 hover:from-medical-blue-700 hover:to-medical-teal-700 text-white font-bold py-3 px-4 rounded-xl shadow-md shadow-medical-blue-500/10 transition-all hover:-translate-y-[1px] active:translate-y-0 active:scale-98 disabled:opacity-50 disabled:cursor-not-allowed text-sm mt-4 flex items-center justify-center gap-2 cursor-pointer"
                      disabled={loading}
                    >
                      {loading ? (
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      ) : 'Confirm New Password'}
                    </button>
                  </form>
                )}
              </motion.div>
            )}
          </AnimatePresence>

          {/* GOOGLE OAUTH ROLE MODAL */}
          {showGoogleRoleModal && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-md p-4 animate-in fade-in duration-200">
              <div 
                className="fixed inset-0"
                onClick={() => setShowGoogleRoleModal(false)}
              ></div>
              <div className="w-full max-w-sm bg-white rounded-3xl border border-slate-100 p-6 shadow-2xl z-10 relative animate-in zoom-in-95 duration-200">
                <h4 className="text-lg font-bold text-slate-900 tracking-tight">Select Google Role</h4>
                <p className="text-xs text-slate-500 mt-1.5 leading-relaxed">
                  First-time Google logins require choosing your portal dashboard role.
                </p>

                <div className="grid grid-cols-2 gap-3.5 mt-6">
                  <button
                    type="button"
                    onClick={() => handleGoogleRoleSelect('PATIENT')}
                    className={`border p-4 rounded-2xl flex flex-col items-center gap-2.5 transition-all font-bold text-xs shadow-xs hover:shadow-md cursor-pointer hover:-translate-y-[1px] ${
                      selectedGoogleRole === 'PATIENT' 
                        ? 'border-medical-blue-500 bg-medical-blue-50/30 text-medical-blue-900' 
                        : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700'
                    }`}
                  >
                    <UserCheck className="text-medical-blue-600" size={24} />
                    <span>Patient Account</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => handleGoogleRoleSelect('DOCTOR')}
                    className={`border p-4 rounded-2xl flex flex-col items-center gap-2.5 transition-all font-bold text-xs shadow-xs hover:shadow-md cursor-pointer hover:-translate-y-[1px] ${
                      selectedGoogleRole === 'DOCTOR' 
                        ? 'border-medical-teal-500 bg-medical-teal-50/30 text-medical-teal-900' 
                        : 'border-slate-200 bg-white hover:bg-slate-50 text-slate-700'
                    }`}
                  >
                    <Stethoscope className="text-medical-teal-600" size={24} />
                    <span>Doctor Account</span>
                  </button>
                </div>

                 <div className="mt-6 flex flex-col items-center justify-center border-t border-slate-100 pt-5 w-full">
                  <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-3">
                    Continue as {selectedGoogleRole === 'DOCTOR' ? 'Doctor' : 'Patient'}
                  </p>
                  <button
                    type="button"
                    onClick={handleFirebaseGoogleSignIn}
                    className="w-full flex items-center justify-center gap-2.5 bg-white hover:bg-slate-50 border border-slate-350 text-slate-700 font-extrabold text-xs py-2.5 px-4 rounded-xl transition-all shadow-sm active:scale-98 cursor-pointer select-none"
                  >
                    <svg className="w-4 h-4" viewBox="0 0 24 24">
                      <path fill="#EA4335" d="M12 5.04c1.66 0 3.2.57 4.38 1.69l3.27-3.27C17.68 1.54 14.98 1 12 1 7.35 1 3.37 3.67 1.39 7.56l3.85 2.99c.9-2.7 3.42-4.51 6.76-4.51z"/>
                      <path fill="#4285F4" d="M23.49 12.27c0-.81-.07-1.59-.2-2.34H12v4.47h6.47c-.29 1.51-1.14 2.78-2.4 3.62l3.71 2.87c2.17-2 3.71-4.94 3.71-8.62z"/>
                      <path fill="#FBBC05" d="M5.24 10.55c-.24-.72-.38-1.49-.38-2.28 0-.79.14-1.56.38-2.28L1.39 3.01C.5 4.81 0 6.85 0 9s.5 4.19 1.39 5.99l3.85-2.99c-.24-.72-.38-1.49-.38-2.28z"/>
                      <path fill="#34A853" d="M12 23c3.24 0 5.97-1.07 7.96-2.91l-3.71-2.87c-1.04.7-2.38 1.12-4.25 1.12-3.34 0-5.86-1.81-6.87-4.51L1.28 16.82C3.26 20.33 7.24 23 12 23z"/>
                    </svg>
                    <span>Continue with Google</span>
                  </button>
                </div>

                <button
                  type="button"
                  onClick={() => setShowGoogleRoleModal(false)}
                  className="w-full mt-5 py-2.5 border-0 bg-slate-100 hover:bg-slate-200 text-slate-700 font-extrabold text-xs rounded-xl transition-colors cursor-pointer active:scale-98"
                >
                  Cancel Selection
                </button>
              </div>
            </div>
          )}

        </div>
      </div>
    </div>
  );
};

export default Auth;

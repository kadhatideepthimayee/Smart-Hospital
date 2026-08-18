import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { updateProfile } from '../../api/auth';
import { Card, Button, Toast } from '../../components/UI';
import { User, Mail, Phone, ShieldCheck } from 'lucide-react';

const PatientProfile: React.FC = () => {
  const { user, updateProfileState } = useAuth();
  
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phone, setPhone] = useState(user?.phone || '');
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim()) {
      setToast({ message: 'Full name field cannot be empty.', type: 'error' });
      return;
    }
    
    setLoading(true);
    try {
      const updatedUser = await updateProfile(fullName.trim(), phone.trim());
      updateProfileState(updatedUser);
      setToast({ message: 'Profile updated successfully.', type: 'success' });
      setTimeout(() => setToast(null), 3000);
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to update profile.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    } finally {
      setLoading(false);
    }
  };

  const getInitials = (name?: string) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="max-w-xl mx-auto space-y-6 animate-in fade-in duration-300">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      <Card className="p-7 rounded-3xl" hoverEffect={false}>
        <div className="flex items-center gap-4 mb-6 border-b border-slate-100 pb-5 select-none">
          <div className="w-16 h-16 rounded-2xl bg-medical-blue-50/70 border border-medical-blue-100/50 text-medical-blue-600 flex items-center justify-center font-black text-xl uppercase shadow-xs">
            {getInitials(user?.fullName)}
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <h3 className="text-base font-extrabold text-slate-900 leading-tight">{user?.fullName}</h3>
              <ShieldCheck size={16} className="text-emerald-500 flex-shrink-0" />
            </div>
            <span className="text-[10px] text-slate-400 font-extrabold uppercase tracking-widest mt-1 block">{user?.role} ACCOUNT</span>
          </div>
        </div>

        <form onSubmit={handleUpdate} className="space-y-4">
          
          {/* Full Name */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Full Name</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <User size={16} />
              </div>
              <input
                type="text"
                className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                disabled={loading}
              />
            </div>
          </div>

          {/* Phone Number */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">Phone Number</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Phone size={16} />
              </div>
              <input
                type="tel"
                maxLength={10}
                className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 outline-none transition-all duration-200"
                value={phone}
                onChange={(e) => setPhone(e.target.value.replace(/\D/g, ''))}
                placeholder="10-digit phone number"
                disabled={loading}
              />
            </div>
          </div>

          {/* Email (Read only) */}
          <div className="space-y-1.5 select-none">
            <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider">Email Address (Read-only)</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-350">
                <Mail size={16} />
              </div>
              <input
                type="email"
                className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-150 text-sm font-semibold bg-slate-50 text-slate-400 cursor-not-allowed outline-none"
                value={user?.email || ''}
                readOnly
              />
            </div>
          </div>

          <Button 
            type="submit" 
            className="w-full mt-6 py-2.5 text-xs rounded-xl font-bold shadow-md hover:-translate-y-0.5 active:translate-y-0" 
            loading={loading}
          >
            Save Profile Changes
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default PatientProfile;

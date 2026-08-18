import React, { useState, useEffect } from 'react';
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { getDoctorProfile } from '../api/doctors';
import { getUnreadNotificationsCount } from '../api/dashboard';
import { getAdminUnreadCount } from '../api/admin';
import { 
  LayoutDashboard, 
  CalendarRange, 
  ListOrdered, 
  User, 
  Bell, 
  LogOut, 
  Menu, 
  X, 
  ShieldAlert, 
  Users, 
  CalendarCheck, 
  Clock, 
  ShieldCheck, 
  FileText
} from 'lucide-react';

export const Layout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  // Fetch Doctor Profile for verification status checks
  const { data: doctorProfile } = useQuery({
    queryKey: ['doctorProfile'],
    queryFn: getDoctorProfile,
    enabled: user?.role === 'DOCTOR',
    retry: false
  });

  // Redirect unverified doctors who try to access other routes
  useEffect(() => {
    if (!user) return;
    
    if (user.role === 'DOCTOR' && doctorProfile) {
      const isVerified = doctorProfile.verificationStatus === 'VERIFIED' || doctorProfile.verificationStatus === 'APPROVED';
      const allowedPaths = ['/doctor/dashboard', '/doctor/profile'];
      const isAllowed = allowedPaths.some(p => location.pathname.startsWith(p));
      
      if (!isVerified && !isAllowed) {
        navigate('/doctor/dashboard', { replace: true });
      }
    }
  }, [user, doctorProfile, location.pathname, navigate]);

  // Periodic polling for unread notifications count
  useEffect(() => {
    if (!user) return;
    const fetchUnread = async () => {
      try {
        if (user.role === 'ADMIN') {
          const data = await getAdminUnreadCount();
          setUnreadCount(data.count || 0);
        } else {
          const data = await getUnreadNotificationsCount();
          setUnreadCount(data.count || 0);
        }
      } catch (err) {
        console.error('Failed to load notifications count', err);
      }
    };
    
    fetchUnread();
    const interval = setInterval(fetchUnread, 15000); // 15s poll
    return () => clearInterval(interval);
  }, [user]);

  const getMenuLinks = () => {
    if (!user) return [];
    
    if (user.role === 'PATIENT') {
      return [
        { path: '/patient/dashboard', label: 'Dashboard', icon: LayoutDashboard },
        { path: '/patient/book', label: 'Book Appointment', icon: CalendarRange },
        { path: '/patient/appointments', label: 'My Appointments', icon: ListOrdered },
        { path: '/patient/queue', label: 'Live Queue', icon: Clock },
        { path: '/patient/records', label: 'Medical Records', icon: FileText },
        { path: '/patient/notifications', label: 'Notifications', icon: Bell, count: unreadCount },
        { path: '/patient/profile', label: 'Profile', icon: User }
      ];
    }

    if (user.role === 'DOCTOR') {
      const isVerified = doctorProfile?.verificationStatus === 'VERIFIED' || doctorProfile?.verificationStatus === 'APPROVED';
      if (!isVerified) {
        return [
          { path: '/doctor/dashboard', label: 'Dashboard', icon: LayoutDashboard },
          { path: '/doctor/profile', label: 'Profile Verification', icon: User }
        ];
      }
      return [
        { path: '/doctor/dashboard', label: 'Dashboard', icon: LayoutDashboard },
        { path: '/doctor/appointments', label: 'Appointments', icon: CalendarCheck },
        { path: '/doctor/queue', label: 'Live Queue', icon: Clock },
        { path: '/doctor/patients', label: 'Patients', icon: Users },
        { path: '/doctor/availability', label: 'Availability', icon: Clock },
        { path: '/doctor/notifications', label: 'Notifications', icon: Bell, count: unreadCount },
        { path: '/doctor/profile', label: 'Profile', icon: User }
      ];
    }

    if (user.role === 'ADMIN') {
      return [
        { path: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
        { path: '/admin/verification', label: 'Doctor Verification', icon: ShieldAlert },
        { path: '/admin/doctors', label: 'Doctors Directory', icon: ShieldCheck },
        { path: '/admin/patients', label: 'Patients Directory', icon: Users },
        { path: '/admin/appointments', label: 'All Appointments', icon: CalendarCheck },
        { path: '/admin/notifications', label: 'Notifications', icon: Bell, count: unreadCount },
        { path: '/admin/profile', label: 'Settings', icon: User }
      ];
    }

    return [];
  };

  const menuLinks = getMenuLinks();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getPageTitle = () => {
    const active = menuLinks.find(link => location.pathname.startsWith(link.path));
    return active ? active.label : 'MedPlus';
  };
  
  const getPageSubtitle = () => {
    if (!user) return '';
    if (user.role === 'PATIENT') return 'MedPlus Patient Workspace';
    if (user.role === 'DOCTOR') return 'Doctor Consultation Workspace';
    if (user.role === 'ADMIN') return 'System Administrator Console';
    return '';
  };

  const getInitials = (name?: string) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <div className="min-h-screen flex bg-slate-50">
      
      {/* Sidebar overlay for mobile drawer */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-slate-900/30 backdrop-blur-xs z-40 md:hidden" 
          onClick={() => setIsOpen(false)}
        ></div>
      )}

      {/* SIDEBAR NAVIGATION SHELL */}
      <aside className={`w-[270px] bg-slate-950 border-r border-slate-900 flex flex-col justify-between fixed md:sticky top-0 h-screen z-50 transition-all duration-300 md:left-0 ${
        isOpen ? 'left-0' : '-left-[270px]'
      }`}>
        
        {/* Header Branding */}
        <div>
          <div className="h-20 flex items-center justify-between px-6 border-b border-slate-900/60">
            <div className="flex items-center gap-3 select-none">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-medical-blue-600 to-medical-teal-500 flex items-center justify-center text-white font-bold shadow-lg shadow-medical-blue-500/20">
                <svg className="w-5.5 h-5.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3.5} d="M19 10.5h-5.5V5h-3v5.5H5v3h5.5V19h3v-5.5H19v-3z" />
                </svg>
              </div>
              <span className="text-xl font-bold tracking-tight text-white">Med<span className="text-medical-teal-400">Plus</span></span>
            </div>
            <button 
              onClick={() => setIsOpen(false)} 
              className="md:hidden p-1.5 text-slate-400 hover:text-slate-200 transition-colors bg-transparent border-0 cursor-pointer rounded-lg hover:bg-slate-900"
            >
              <X size={18} />
            </button>
          </div>

          {/* Navigation Menu */}
          <nav className="p-4 space-y-1 overflow-y-auto max-h-[calc(100vh-170px)] scrollbar-thin">
            {menuLinks.map(link => {
              const IconComp = link.icon;
              return (
                <NavLink
                  key={link.path}
                  to={link.path}
                  onClick={() => setIsOpen(false)}
                  className={({ isActive }) => `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 font-semibold text-sm ${
                    isActive 
                      ? 'bg-gradient-to-r from-medical-blue-600 to-medical-teal-600 text-white shadow-md shadow-medical-blue-600/20' 
                      : 'text-slate-400 hover:bg-slate-900 hover:text-slate-200'
                  }`}
                >
                  <IconComp size={18} className="flex-shrink-0" />
                  <span className="flex-1">{link.label}</span>
                  {link.count !== undefined && link.count > 0 && (
                    <span className="bg-rose-600 text-white text-[10px] px-2 py-0.5 rounded-full font-extrabold animate-pulse">
                      {link.count}
                    </span>
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Sidebar Footer User Details */}
        <div className="p-4 border-t border-slate-900/60 space-y-3 bg-slate-950/20">
          <div className="flex items-center gap-3 px-2 py-1">
            <div className="w-10 h-10 rounded-xl bg-slate-900 text-slate-200 flex items-center justify-center font-bold text-sm uppercase select-none border border-slate-800 shadow-inner">
              {getInitials(user?.fullName)}
            </div>
            <div className="flex-1 overflow-hidden select-none">
              <div className="text-xs font-bold text-slate-200 truncate leading-snug">{user?.fullName}</div>
              <div className="text-[9px] font-bold text-slate-500 uppercase tracking-widest mt-0.5">{user?.role}</div>
            </div>
          </div>
          
          <button 
            onClick={handleLogout} 
            className="w-full flex items-center gap-2.5 px-4 py-2.5 rounded-xl text-rose-500 hover:bg-rose-500/10 transition-colors bg-transparent border-0 text-left font-bold text-xs cursor-pointer active:scale-98"
          >
            <LogOut size={16} />
            <span>Logout Session</span>
          </button>
        </div>
      </aside>

      {/* MAIN CONTENT WRAPPER */}
      <div className="flex-1 flex flex-col min-w-0">
        
        {/* Desktop Navbar */}
        <header className="h-20 bg-white border-b border-slate-100 flex items-center justify-between px-6 md:px-8 sticky top-0 z-35 shadow-[0_1px_3px_rgba(0,0,0,0.02)]">
          {/* Hamburger (Mobile) */}
          <button 
            onClick={() => setIsOpen(true)} 
            className="md:hidden p-2 -ml-2 text-slate-500 hover:text-slate-800 transition-colors bg-transparent border-0 cursor-pointer hover:bg-slate-50 rounded-lg"
          >
            <Menu size={22} />
          </button>

          {/* Page Titles / Breadcrumbs */}
          <div className="flex flex-col ml-2 md:ml-0">
            <h2 className="text-lg font-bold text-slate-900 leading-tight tracking-tight">{getPageTitle()}</h2>
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mt-1">{getPageSubtitle()}</span>
          </div>
          
          {/* Actions Menu */}
          <div className="flex items-center gap-4">
            
            {/* Notifications Bell */}
            <NavLink 
              to={user?.role === 'ADMIN' ? '/admin/notifications' : `/${user?.role?.toLowerCase()}/notifications`}
              className="p-2.5 text-slate-500 hover:text-slate-850 hover:bg-slate-50 rounded-xl transition-all duration-200 bg-transparent border-0 relative cursor-pointer"
              title="Notifications"
            >
              <Bell size={20} />
              {unreadCount > 0 && (
                <span className="absolute top-2 right-2 w-2 h-2 bg-rose-600 rounded-full ring-2 ring-white animate-pulse"></span>
              )}
            </NavLink>

            {/* Profile trigger */}
            <NavLink 
              to={user?.role === 'ADMIN' ? '/admin/profile' : `/${user?.role?.toLowerCase()}/profile`}
              className="flex items-center gap-3 border-l border-slate-150 pl-4 cursor-pointer group"
              title="View Profile"
            >
              <div className="w-9 h-9 rounded-xl bg-medical-blue-50/70 text-medical-blue-600 font-bold text-xs uppercase flex items-center justify-center group-hover:bg-medical-blue-600 group-hover:text-white transition-all duration-200 select-none shadow-xs border border-medical-blue-100/50">
                {getInitials(user?.fullName)}
              </div>
              <div className="hidden sm:flex flex-col">
                <span className="text-xs font-bold text-slate-800 leading-none group-hover:text-medical-blue-650 transition-colors">{user?.fullName}</span>
                <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider mt-1">{user?.role}</span>
              </div>
            </NavLink>
          </div>
        </header>

        {/* Content Body */}
        <main className="flex-1 p-6 md:p-8 overflow-y-auto">
          <div className="max-w-7xl mx-auto fade-in">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default Layout;

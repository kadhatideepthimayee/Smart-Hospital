import React from 'react';
import { Activity } from 'lucide-react';

interface SplashProps {
  message?: string;
}

export const Splash: React.FC<SplashProps> = ({ 
  message = 'Synchronizing with MedPlus secure servers...' 
}) => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
      <div className="text-center max-w-sm">
        
        {/* Healthcare Logo Box */}
        <div className="mx-auto w-24 h-24 rounded-3xl bg-gradient-to-tr from-medical-blue-600 to-medical-teal-500 flex items-center justify-center text-white shadow-xl shadow-medical-blue-500/10 mb-8 relative select-none">
          {/* Pulsing Outer Rings */}
          <div className="absolute inset-0 rounded-3xl bg-medical-blue-500 opacity-20 animate-ping duration-1500"></div>
          <div className="absolute -inset-2 rounded-3xl bg-medical-teal-400/10 animate-pulse duration-1000"></div>
          <svg className="w-12 h-12 relative z-10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3.5} d="M19 10.5h-5.5V5h-3v5.5H5v3h5.5V19h3v-5.5H19v-3z" />
          </svg>
        </div>

        {/* Application Name */}
        <h1 className="text-3xl font-extrabold text-slate-900 mb-1 tracking-tight select-none">
          Med<span className="text-medical-teal-600">Plus</span>
        </h1>
        <p className="text-[10px] text-slate-400 font-bold mb-10 tracking-widest uppercase select-none">
          Smart Hospital Queue Management
        </p>

        {/* Dynamic Spinner and Status */}
        <div className="flex flex-col items-center gap-4">
          <div className="w-10 h-10 border-[3.5px] border-slate-100 border-t-medical-blue-600 rounded-full animate-spin"></div>
          <p className="text-xs text-slate-500 font-semibold flex items-center gap-2 mt-2 animate-pulse select-none bg-white py-2 px-4 rounded-full border border-slate-100 shadow-xs">
            <Activity className="w-4 h-4 text-medical-teal-500" />
            {message}
          </p>
        </div>
      </div>
    </div>
  );
};

export default Splash;

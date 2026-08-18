import React, { useEffect } from 'react';
import { cn } from '../lib/utils';
import { X, CheckCircle, AlertTriangle, Info } from 'lucide-react';

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  hoverEffect?: boolean;
}

export const Card: React.FC<CardProps> = ({ children, className, hoverEffect = true, ...props }) => (
  <div 
    className={cn(
      "bg-white border border-slate-100 rounded-2xl p-6 shadow-[0_2px_8px_-3px_rgba(0,0,0,0.05),0_8px_20px_-12px_rgba(0,0,0,0.03)] transition-all duration-300", 
      hoverEffect && "hover:shadow-[0_8px_30px_rgb(0,0,0,0.04)] hover:border-slate-200/80 hover:-translate-y-[2px]",
      className
    )} 
    {...props}
  >
    {children}
  </div>
);

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost';
  loading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({ 
  children, 
  className, 
  variant = 'primary', 
  loading = false, 
  disabled = false, 
  ...props 
}) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'secondary':
        return 'bg-slate-100 hover:bg-slate-200 active:bg-slate-300 text-slate-800 border border-transparent shadow-xs';
      case 'danger':
        return 'bg-rose-600 hover:bg-rose-700 active:bg-rose-800 text-white border border-transparent shadow-md shadow-rose-500/10 hover:shadow-lg hover:shadow-rose-500/20';
      case 'outline':
        return 'bg-white hover:bg-slate-50 active:bg-slate-100 text-slate-700 border border-slate-200 hover:border-slate-350 shadow-xs';
      case 'ghost':
        return 'bg-transparent hover:bg-slate-50 active:bg-slate-100 text-slate-600 hover:text-slate-900 border border-transparent';
      default: // primary
        return 'bg-gradient-to-r from-medical-blue-600 to-medical-teal-600 hover:from-medical-blue-700 hover:to-medical-teal-700 active:from-medical-blue-800 active:to-medical-teal-800 text-white border border-transparent shadow-md shadow-medical-blue-500/10 hover:shadow-lg hover:shadow-medical-blue-500/20';
    }
  };

  return (
    <button 
      className={cn(
        "inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl font-semibold text-sm transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-medical-blue-500/15 disabled:opacity-50 disabled:cursor-not-allowed select-none cursor-pointer hover:-translate-y-[1px] active:translate-y-0 active:scale-[0.98]",
        getVariantStyles(),
        className
      )} 
      disabled={loading || disabled} 
      {...props}
    >
      {loading && (
        <span className="inline-block w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></span>
      )}
      {children}
    </button>
  );
};

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-md p-4 animate-in fade-in duration-200">
      <div 
        className="fixed inset-0" 
        onClick={onClose}
      ></div>
      <div 
        className="bg-white rounded-3xl border border-slate-100 p-6 shadow-2xl w-full max-w-lg z-10 relative animate-in zoom-in-95 duration-200 flex flex-col max-h-[90vh]"
      >
        <div className="flex items-center justify-between border-b border-slate-100 pb-4 mb-4 flex-shrink-0">
          <h3 className="text-lg font-bold text-slate-900 tracking-tight">{title}</h3>
          <button 
            onClick={onClose} 
            className="p-1.5 text-slate-400 hover:text-slate-650 hover:bg-slate-100 rounded-xl transition-all bg-transparent border-0 cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>
        <div className="overflow-y-auto flex-1 pr-1 scrollbar-thin">
          {children}
        </div>
      </div>
    </div>
  );
};

interface StatusBadgeProps {
  status?: string;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className }) => {
  const getBadgeColors = () => {
    switch (status) {
      case 'COMPLETED':
      case 'ACTIVE':
      case 'VERIFIED':
      case 'APPROVED':
        return {
          bg: 'bg-emerald-50/70 text-emerald-700 border-emerald-100/70',
          dot: 'bg-emerald-500'
        };
      case 'IN_PROGRESS':
      case 'WAITING':
      case 'PENDING':
        return {
          bg: 'bg-amber-50/70 text-amber-700 border-amber-100/70',
          dot: 'bg-amber-500'
        };
      case 'CANCELLED':
      case 'REJECTED':
      case 'INACTIVE':
      case 'DOCTOR_RUNNING_LATE':
        return {
          bg: 'bg-rose-50/70 text-rose-700 border-rose-100/70',
          dot: 'bg-rose-500'
        };
      default:
        return {
          bg: 'bg-blue-50/70 text-blue-700 border-blue-100/70',
          dot: 'bg-blue-500'
        };
    }
  };

  const { bg, dot } = getBadgeColors();

  return (
    <span 
      className={cn(
        "inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-bold border uppercase tracking-wide select-none", 
        bg,
        className
      )}
    >
      <span className={cn(
        "w-1.5 h-1.5 rounded-full", 
        dot, 
        (status === 'IN_PROGRESS' || status === 'WAITING' || status === 'DOCTOR_RUNNING_LATE') && "animate-pulse"
      )}></span>
      {status ? status.replace('_', ' ') : 'UNKNOWN'}
    </span>
  );
};

interface SkeletonProps {
  className?: string;
  height?: string;
  width?: string;
}

export const Skeleton: React.FC<SkeletonProps> = ({ className, height = '20px', width = '100%' }) => (
  <div 
    className={cn("bg-slate-100 animate-pulse rounded-2xl", className)} 
    style={{ height, width }}
  />
);

interface ToastProps {
  message: string;
  type?: 'success' | 'error' | 'info';
  onClose: () => void;
}

export const Toast: React.FC<ToastProps> = ({ message, type = 'info', onClose }) => {
  useEffect(() => {
    const timer = setTimeout(onClose, 4000);
    return () => clearTimeout(timer);
  }, [onClose]);

  const getTypeStyles = () => {
    switch (type) {
      case 'success': 
        return 'bg-white border-emerald-100 text-slate-800 shadow-emerald-500/5';
      case 'error': 
        return 'bg-white border-rose-100 text-slate-800 shadow-rose-500/5';
      default: 
        return 'bg-white border-blue-100 text-slate-800 shadow-blue-500/5';
    }
  };

  const getIcon = () => {
    switch (type) {
      case 'success': return <CheckCircle size={18} className="text-emerald-500 flex-shrink-0" />;
      case 'error': return <AlertTriangle size={18} className="text-rose-500 flex-shrink-0" />;
      default: return <Info size={18} className="text-blue-500 flex-shrink-0" />;
    }
  };

  return (
    <div 
      className={cn(
        "fixed bottom-6 right-6 p-4 border rounded-2xl flex items-center gap-3.5 shadow-[0_10px_30px_rgba(0,0,0,0.06)] z-[999] max-w-sm animate-in fade-in slide-in-from-bottom-4 duration-300 font-semibold text-sm", 
        getTypeStyles()
      )}
    >
      {getIcon()}
      <span className="flex-1">{message}</span>
      <button 
        onClick={onClose} 
        className="p-1 text-slate-400 hover:text-slate-650 bg-transparent border-0 cursor-pointer rounded-lg hover:bg-slate-55 transition-colors"
      >
        <X size={16} />
      </button>
    </div>
  );
};

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(({
  label,
  error,
  icon,
  className,
  ...props
}, ref) => {
  return (
    <div className="space-y-1.5 w-full">
      {label && (
        <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider select-none">
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            {icon}
          </div>
        )}
        <input
          ref={ref}
          className={cn(
            "w-full pr-4 py-2.5 rounded-xl border text-sm font-medium focus:ring-4 focus:ring-medical-blue-500/10 focus:border-medical-blue-500 transition-all duration-200 outline-none bg-white",
            icon ? "pl-10" : "pl-4",
            error ? "border-rose-500" : "border-slate-200 hover:border-slate-300/80",
            className
          )}
          {...props}
        />
      </div>
      {error && (
        <p className="text-xs text-rose-600 font-semibold flex items-center gap-1 select-none animate-in fade-in slide-in-from-top-1 duration-150">
          <AlertTriangle size={12} className="text-rose-500" />
          {error}
        </p>
      )}
    </div>
  );
});
Input.displayName = 'Input';

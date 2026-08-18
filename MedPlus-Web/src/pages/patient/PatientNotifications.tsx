import React, { useState } from 'react';
import { useNotifications } from '../../hooks/useNotifications';
import { Card, Button, Skeleton, Toast } from '../../components/UI';
import { Bell, BellOff, Check, Trash2, Calendar, Clock, AlertTriangle, MessageSquare } from 'lucide-react';

const PatientNotifications: React.FC = () => {
  const { data: notifications = [], isLoading, markAsRead, markAllAsRead, deleteNotification } = useNotifications();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const handleMarkReadClick = async (id: string) => {
    try {
      await markAsRead(id);
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to update status', type: 'error' });
    }
  };

  const handleMarkAllReadClick = async () => {
    try {
      await markAllAsRead();
      setToast({ message: 'All notifications marked as read.', type: 'success' });
      setTimeout(() => setToast(null), 3000);
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to update status', type: 'error' });
    }
  };

  const handleDeleteClick = async (id: string) => {
    try {
      await deleteNotification(id);
      setToast({ message: 'Notification deleted.', type: 'success' });
      setTimeout(() => setToast(null), 3000);
    } catch (err: any) {
      setToast({ message: err.message || 'Failed to delete notification', type: 'error' });
    }
  };

  const getIcon = (type: string) => {
    switch (type) {
      case 'DELAY':
      case 'WARNING':
        return <AlertTriangle className="text-amber-500" size={16} />;
      case 'BOOKING':
      case 'APPOINTMENT':
        return <Calendar className="text-medical-blue-500" size={16} />;
      case 'FEEDBACK':
        return <MessageSquare className="text-purple-500" size={16} />;
      default:
        return <Bell className="text-slate-400" size={16} />;
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Toast Alert */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-5 select-none">
        <p className="text-xs font-bold text-slate-400">
          Check up-to-date schedule changes, queue shifts, and confirmation cards.
        </p>
        {notifications.some(n => !n.isRead) && (
          <Button 
            onClick={handleMarkAllReadClick} 
            variant="outline" 
            className="py-2 px-4 text-xs rounded-xl font-bold shadow-xs hover:-translate-y-0.5 active:translate-y-0"
          >
            Mark all read
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="space-y-3">
          <Skeleton height="80px" className="rounded-2xl" />
          <Skeleton height="80px" className="rounded-2xl" />
        </div>
      ) : notifications.length > 0 ? (
        <div className="space-y-3.5">
          {notifications.map(notif => {
            const notifId = notif.id || notif._id;
            return (
              <div 
                key={notifId} 
                className={`p-5 rounded-2xl border transition-all duration-200 flex items-start justify-between gap-4 ${
                  notif.isRead 
                    ? 'bg-white border-slate-100/75 shadow-xs' 
                    : 'bg-medical-blue-50/30 border-medical-blue-100/70 shadow-sm shadow-medical-blue-500/5'
                }`}
              >
                <div className="flex gap-4">
                  <div className="p-2.5 bg-white border border-slate-100 rounded-xl shadow-xs flex-shrink-0 self-start select-none">
                    {getIcon(notif.type)}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h4 className="font-extrabold text-sm text-slate-800 leading-snug">{notif.title}</h4>
                      {!notif.isRead && (
                        <span className="w-1.5 h-1.5 rounded-full bg-medical-blue-600 flex-shrink-0" title="Unread"></span>
                      )}
                    </div>
                    <p className="text-xs text-slate-500 mt-1 mb-2 leading-relaxed font-semibold">{notif.message}</p>
                    <span className="text-[9px] text-slate-400 font-bold uppercase select-none flex items-center gap-1.5">
                      <Clock size={11} className="text-slate-350" />
                      {new Date(notif.timestamp).toLocaleString(undefined, { month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric', hour12: true })}
                    </span>
                  </div>
                </div>

                <div className="flex gap-2 select-none flex-shrink-0">
                  {!notif.isRead && (
                    <button 
                      onClick={() => handleMarkReadClick(notifId)} 
                      title="Mark as read"
                      className="p-2 border border-slate-200 rounded-xl hover:bg-emerald-50 hover:text-emerald-700 hover:border-emerald-200 text-slate-450 bg-white cursor-pointer transition-colors outline-none active:scale-95"
                    >
                      <Check size={14} />
                    </button>
                  )}
                  <button 
                    onClick={() => handleDeleteClick(notifId)} 
                    title="Delete notification"
                    className="p-2 border border-slate-200 rounded-xl hover:bg-rose-50 hover:text-rose-700 hover:border-rose-200 text-slate-450 bg-white cursor-pointer transition-colors outline-none active:scale-95"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <Card className="text-center py-16 max-w-md mx-auto select-none rounded-3xl p-8" hoverEffect={true}>
          <div className="w-14 h-14 bg-slate-50 text-slate-400 rounded-2xl flex items-center justify-center mx-auto mb-4 border border-slate-100/70 shadow-xs">
            <BellOff size={24} />
          </div>
          <h3 className="text-base font-extrabold text-slate-800">Inbox Empty</h3>
          <p className="text-xs text-slate-450 font-semibold mt-2">No notifications at the moment.</p>
        </Card>
      )}
    </div>
  );
};

export default PatientNotifications;

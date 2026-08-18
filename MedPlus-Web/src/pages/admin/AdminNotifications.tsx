import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAdminNotifications, markAdminNotificationRead, deleteAdminNotification } from '../../api/admin';
import { Card, Button, Skeleton, Toast } from '../../components/UI';
import { Bell, BellOff, Check, Trash2, ShieldAlert, Clock } from 'lucide-react';

const AdminNotifications: React.FC = () => {
  const queryClient = useQueryClient();
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // Fetch admin notifications
  const { data: notifications = [], isLoading } = useQuery({
    queryKey: ['adminNotifications'],
    queryFn: getAdminNotifications,
  });

  // Mark notification as read mutation
  const markReadMutation = useMutation({
    mutationFn: markAdminNotificationRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminNotifications'] });
      queryClient.invalidateQueries({ queryKey: ['adminUnreadCount'] });
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to update notification.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  // Delete notification mutation
  const deleteMutation = useMutation({
    mutationFn: deleteAdminNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminNotifications'] });
      queryClient.invalidateQueries({ queryKey: ['adminUnreadCount'] });
      setToast({ message: 'Notification deleted.', type: 'success' });
      setTimeout(() => setToast(null), 3000);
    },
    onError: (err: any) => {
      setToast({ message: err.message || 'Failed to delete notification.', type: 'error' });
      setTimeout(() => setToast(null), 4000);
    }
  });

  const handleMarkRead = (id: string) => {
    markReadMutation.mutate(id);
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id);
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Toast popup */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-5 select-none">
        <p className="text-xs font-bold text-slate-400">
          Check admin log updates and verification requests.
        </p>
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
                    <ShieldAlert className="text-amber-500" size={16} />
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
                      <Clock size={11} className="text-slate-355" />
                      {new Date(notif.timestamp).toLocaleString(undefined, { month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: 'numeric', hour12: true })}
                    </span>
                  </div>
                </div>

                <div className="flex gap-2 select-none flex-shrink-0">
                  {!notif.isRead && (
                    <button 
                      onClick={() => handleMarkRead(notifId)} 
                      title="Mark as read"
                      className="p-2 border border-slate-200 rounded-xl hover:bg-emerald-50 hover:text-emerald-700 hover:border-emerald-200 text-slate-450 bg-white cursor-pointer transition-colors outline-none active:scale-95"
                    >
                      <Check size={14} />
                    </button>
                  )}
                  <button 
                    onClick={() => handleDelete(notifId)} 
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
          <h3 className="text-base font-extrabold text-slate-850">Inbox Empty</h3>
          <p className="text-xs text-slate-450 font-semibold mt-2">No admin logs at the moment.</p>
        </Card>
      )}
    </div>
  );
};

export default AdminNotifications;

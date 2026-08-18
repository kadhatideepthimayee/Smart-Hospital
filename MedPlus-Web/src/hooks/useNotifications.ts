import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getNotifications, markNotificationAsRead, markAllNotificationsAsRead, deleteNotification } from '../api/dashboard';

export const useNotifications = () => {
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ['notifications'],
    queryFn: getNotifications,
    refetchInterval: 20000, // Poll every 20 seconds
  });

  const markReadMutation = useMutation({
    mutationFn: markNotificationAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationsCount'] });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: markAllNotificationsAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationsCount'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationsCount'] });
    },
  });

  return {
    ...query,
    markAsRead: markReadMutation.mutateAsync,
    markAllAsRead: markAllReadMutation.mutateAsync,
    deleteNotification: deleteMutation.mutateAsync,
  };
};

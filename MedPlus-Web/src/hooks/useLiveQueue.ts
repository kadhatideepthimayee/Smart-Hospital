import { useQuery } from '@tanstack/react-query';
import { getLiveQueueTracking } from '../api/dashboard';

export const useLiveQueue = (appointmentId?: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['liveQueue', appointmentId],
    queryFn: () => getLiveQueueTracking(appointmentId),
    refetchInterval: 12000, // Poll every 12 seconds
    enabled,
    refetchOnWindowFocus: true,
  });
};

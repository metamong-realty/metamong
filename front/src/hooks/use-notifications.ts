import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import { useAuth } from '@/lib/auth-context';

export interface Notification {
  id: number;
  complexId: number | null;
  complexName: string | null;
  exclusivePyeong: number | null;
  price: number | null;
  contractDate: string | null;
  isRead: boolean;
  createdAt: string;
}

export interface UnreadCountResponse {
  count: number;
}

export function useNotifications() {
  const { accessToken } = useAuth();

  return useQuery({
    queryKey: ['notifications'],
    queryFn: () =>
      apiFetch<Notification[]>('/v1/notifications', {
        headers: { Authorization: `Bearer ${accessToken}` },
      }),
    enabled: !!accessToken,
    refetchInterval: 60_000, // 1분마다 폴링
  });
}

export function useUnreadCount() {
  const { accessToken } = useAuth();

  return useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: () =>
      apiFetch<UnreadCountResponse>('/v1/notifications/unread-count', {
        headers: { Authorization: `Bearer ${accessToken}` },
      }),
    enabled: !!accessToken,
    refetchInterval: 60_000,
  });
}

export function useMarkAsRead() {
  const { accessToken } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) =>
      apiFetch(`/v1/notifications/${id}/read`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${accessToken}` },
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}

export function useMarkAllAsRead() {
  const { accessToken } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () =>
      apiFetch('/v1/notifications/read-all', {
        method: 'PUT',
        headers: { Authorization: `Bearer ${accessToken}` },
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });
}

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import { useAuth } from '@/lib/auth-context';

export interface Subscription {
  id: number;
  type: 'COMPLEX' | 'REGION' | 'CONDITION';
  tradeType: 'TRADE' | 'RENT';
  apartmentComplexId: number | null;
  regionCode: string | null;
  isActive: boolean;
  createdAt: string;
}

export function useMySubscriptions() {
  const { accessToken } = useAuth();

  return useQuery({
    queryKey: ['subscriptions'],
    queryFn: () =>
      apiFetch<Subscription[]>('/v1/subscriptions', {
        headers: { Authorization: `Bearer ${accessToken}` },
      }),
    enabled: !!accessToken,
  });
}

export function useSubscribeComplex() {
  const { accessToken } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (complexId: number) =>
      apiFetch<Subscription>('/v1/subscriptions', {
        method: 'POST',
        headers: { Authorization: `Bearer ${accessToken}` },
        body: JSON.stringify({ type: 'COMPLEX', apartmentComplexId: complexId }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
    },
  });
}

export function useUnsubscribe() {
  const { accessToken } = useAuth();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (subscriptionId: number) =>
      apiFetch(`/v1/subscriptions/${subscriptionId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${accessToken}` },
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscriptions'] });
    },
  });
}

import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { Region } from '@/types';

export const useGetSidoList = () =>
  useQuery({
    queryKey: ['regions', 'sido'],
    queryFn: () => apiFetch<Region[]>('/v1/apartments/regions/sido'),
  });

export const useGetSigunguList = (sidoCode: string) =>
  useQuery({
    queryKey: ['regions', 'sigungu', sidoCode],
    queryFn: () => apiFetch<Region[]>(`/v1/apartments/regions/sigungu?sidoCode=${sidoCode}`),
    enabled: !!sidoCode,
  });

export const useGetEupmyeondongList = (sidoCode: string, sigunguCode: string) =>
  useQuery({
    queryKey: ['regions', 'eupmyeondong', sidoCode, sigunguCode],
    queryFn: () =>
      apiFetch<Region[]>(
        `/v1/apartments/regions/eupmyeondong?sidoCode=${sidoCode}&sigunguCode=${sigunguCode}`,
      ),
    enabled: !!sidoCode && !!sigunguCode,
  });

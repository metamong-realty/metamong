import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { RentChartData, TimePeriodFilter, TradeChartData } from '@/types';

interface UseGetChartParams {
  unitTypeId?: number;
  period?: TimePeriodFilter;
}

export const useGetTradeChart = (complexId: number, params: UseGetChartParams = {}) => {
  const { unitTypeId, period = 'RECENT_3YEARS' } = params;

  const searchParams = new URLSearchParams({ period });
  if (unitTypeId) {
    searchParams.set('unitTypeId', String(unitTypeId));
  }

  return useQuery({
    queryKey: ['complexes', complexId, 'trades', 'chart', { unitTypeId, period }],
    queryFn: () =>
      apiFetch<TradeChartData>(
        `/v1/apartments/complexes/${complexId}/trades/chart?${searchParams}`,
      ),
    enabled: complexId > 0,
  });
};

export const useGetRentChart = (complexId: number, params: UseGetChartParams = {}) => {
  const { unitTypeId, period = 'RECENT_3YEARS' } = params;

  const searchParams = new URLSearchParams({ period });
  if (unitTypeId) {
    searchParams.set('unitTypeId', String(unitTypeId));
  }

  return useQuery({
    queryKey: ['complexes', complexId, 'rents', 'chart', { unitTypeId, period }],
    queryFn: () =>
      apiFetch<RentChartData>(`/v1/apartments/complexes/${complexId}/rents/chart?${searchParams}`),
    enabled: complexId > 0,
  });
};

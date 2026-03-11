import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { PaginatedResponse, TimePeriodFilter, Trade } from '@/types';

interface UseGetTradesParams {
  unitTypeId?: number;
  period?: TimePeriodFilter;
  page?: number;
  size?: number;
}

export const useGetTrades = (complexId: number, params: UseGetTradesParams = {}) => {
  const { unitTypeId, period = 'RECENT_3YEARS', page = 0, size = 100 } = params;

  const searchParams = new URLSearchParams({
    period,
    page: String(page),
    size: String(size),
  });
  if (unitTypeId) {
    searchParams.set('unitTypeId', String(unitTypeId));
  }

  return useQuery({
    queryKey: ['complexes', complexId, 'trades', { unitTypeId, period, page, size }],
    queryFn: () =>
      apiFetch<PaginatedResponse<Trade>>(
        `/v1/apartments/complexes/${complexId}/trades?${searchParams}`,
      ),
    enabled: complexId > 0,
  });
};

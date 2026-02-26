import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { PaginatedResponse, Rent, TimePeriodFilter } from '@/types';

interface UseGetRentsParams {
  unitTypeId?: number;
  period?: TimePeriodFilter;
  rentType?: 'JEONSE' | 'MONTHLY_RENT';
  page?: number;
  size?: number;
}

export const useGetRents = (complexId: number, params: UseGetRentsParams = {}) => {
  const { unitTypeId, period = 'RECENT_3YEARS', rentType, page = 0, size = 100 } = params;

  const searchParams = new URLSearchParams({
    period,
    page: String(page),
    size: String(size),
  });
  if (unitTypeId) {
    searchParams.set('unitTypeId', String(unitTypeId));
  }
  if (rentType) {
    searchParams.set('rentType', rentType);
  }

  return useQuery({
    queryKey: ['complexes', complexId, 'rents', { unitTypeId, period, rentType, page, size }],
    queryFn: () =>
      apiFetch<PaginatedResponse<Rent>>(
        `/v1/apartments/complexes/${complexId}/rents?${searchParams}`,
      ),
    enabled: complexId > 0,
  });
};

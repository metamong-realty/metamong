import { keepPreviousData, useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { LookbackMonths, PriceSummary } from '@/types';

interface PriceSummaryParams {
  unitTypeId?: number;
  lookbackMonths?: LookbackMonths;
}

export const useGetPriceSummary = (complexId: number, params: PriceSummaryParams = {}) => {
  const searchParams = new URLSearchParams();
  if (params.unitTypeId) searchParams.set('unitTypeId', String(params.unitTypeId));
  if (params.lookbackMonths) searchParams.set('lookbackMonths', String(params.lookbackMonths));

  const query = searchParams.toString();
  const path = `/v1/apartments/complexes/${complexId}/price-summary${query ? `?${query}` : ''}`;

  return useQuery({
    queryKey: ['complexes', complexId, 'price-summary', params],
    queryFn: () => apiFetch<PriceSummary>(path),
    placeholderData: keepPreviousData,
  });
};

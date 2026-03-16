import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { ComplexListItem, PaginatedResponse } from '@/types';
import type { SortOrder } from '@/components/sort-selector';

interface UseGetComplexesParams {
  sidoSigunguCode: string;
  eupmyeondongCode?: string;
  page?: number;
  size?: number;
  sortOrder?: SortOrder;
}

export const useGetComplexes = ({
  sidoSigunguCode,
  eupmyeondongCode,
  page = 0,
  size = 100,
  sortOrder = 'TRADE_COUNT',
}: UseGetComplexesParams) => {
  const params = new URLSearchParams({
    sidoSigunguCode,
    page: String(page),
    size: String(size),
    sortOrder,
  });
  if (eupmyeondongCode) {
    params.set('eupmyeondongCode', eupmyeondongCode);
  }

  return useQuery({
    queryKey: ['complexes', sidoSigunguCode, eupmyeondongCode, page, size, sortOrder],
    queryFn: () =>
      apiFetch<PaginatedResponse<ComplexListItem>>(`/v1/apartments/complexes?${params}`),
    enabled: !!sidoSigunguCode,
  });
};

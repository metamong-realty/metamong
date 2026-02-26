import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { ComplexListItem, PaginatedResponse } from '@/types';

interface UseGetComplexesParams {
  sidoSigunguCode: string;
  eupmyeondongCode?: string;
  page?: number;
  size?: number;
}

export const useGetComplexes = ({
  sidoSigunguCode,
  eupmyeondongCode,
  page = 0,
  size = 100,
}: UseGetComplexesParams) => {
  const params = new URLSearchParams({
    sidoSigunguCode,
    page: String(page),
    size: String(size),
  });
  if (eupmyeondongCode) {
    params.set('eupmyeondongCode', eupmyeondongCode);
  }

  return useQuery({
    queryKey: ['complexes', sidoSigunguCode, eupmyeondongCode, page, size],
    queryFn: () =>
      apiFetch<PaginatedResponse<ComplexListItem>>(`/v1/apartments/complexes?${params}`),
    enabled: !!sidoSigunguCode,
  });
};

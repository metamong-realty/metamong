import { useInfiniteQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { ComplexListItem, PaginatedResponse } from '@/types';
import type { SortOrder } from '@/components/sort-selector';

const PAGE_SIZE = 30;

interface UseGetComplexesParams {
  sidoSigunguCode: string;
  eupmyeondongCode?: string;
  sortOrder?: SortOrder;
}

export const useGetComplexes = ({
  sidoSigunguCode,
  eupmyeondongCode,
  sortOrder = 'TRADE_COUNT',
}: UseGetComplexesParams) => {
  return useInfiniteQuery({
    queryKey: ['complexes', sidoSigunguCode, eupmyeondongCode, sortOrder],
    queryFn: ({ pageParam = 0 }) => {
      const params = new URLSearchParams({
        sidoSigunguCode,
        page: String(pageParam),
        size: String(PAGE_SIZE),
        sortOrder,
      });
      if (eupmyeondongCode) {
        params.set('eupmyeondongCode', eupmyeondongCode);
      }
      return apiFetch<PaginatedResponse<ComplexListItem>>(`/v1/apartments/complexes?${params}`);
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) => {
      const nextPage = allPages.length;
      return nextPage < lastPage.totalPages ? nextPage : undefined;
    },
    enabled: !!sidoSigunguCode,
  });
};

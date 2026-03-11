import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { ComplexDetail } from '@/types';

export const useGetComplexDetail = (complexId: number) =>
  useQuery({
    queryKey: ['complexes', complexId, 'detail'],
    queryFn: () => apiFetch<ComplexDetail>(`/v1/apartments/complexes/${complexId}`),
    enabled: complexId > 0,
  });

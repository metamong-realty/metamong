import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { UnitType } from '@/types';

export const useGetUnitTypes = (complexId: number) =>
  useQuery({
    queryKey: ['complexes', complexId, 'unitTypes'],
    queryFn: () => apiFetch<UnitType[]>(`/v1/apartments/complexes/${complexId}/unit-types`),
    enabled: complexId > 0,
  });

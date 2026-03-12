import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api-client';
import type { Region } from '@/types';

interface RegionAllResponse {
  sido: Region[];
  sigungu: Record<string, Region[]>;
  eupmyeondong: Record<string, Region[]>;
}

// 전체 지역 데이터를 한 번에 로드
export const useGetAllRegions = () =>
  useQuery({
    queryKey: ['regions', 'all'],
    queryFn: () => apiFetch<RegionAllResponse>('/v1/apartments/regions/all'),
  });

// 시도 목록
export const useGetSidoList = () => {
  const { data: allRegions, isLoading, error } = useGetAllRegions();

  return {
    data: allRegions?.sido ?? [],
    isLoading,
    error,
  };
};

// 시군구 목록 (시도 코드로 필터링)
export const useGetSigunguList = (sidoCode: string) => {
  const { data: allRegions, isLoading, error } = useGetAllRegions();

  return {
    data: sidoCode && allRegions ? (allRegions.sigungu[sidoCode] ?? []) : [],
    isLoading,
    error,
  };
};

// 읍면동 목록 (시도+시군구 코드로 필터링)
export const useGetEupmyeondongList = (sidoCode: string, sigunguCode: string) => {
  const { data: allRegions, isLoading, error } = useGetAllRegions();

  const key = sidoCode && sigunguCode ? sidoCode + sigunguCode : '';

  return {
    data: key && allRegions ? (allRegions.eupmyeondong[key] ?? []) : [],
    isLoading,
    error,
  };
};

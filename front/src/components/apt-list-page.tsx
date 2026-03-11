'use client';

import { Loader2, Search } from 'lucide-react';
import Link from 'next/link';
import { useQueryState } from 'nuqs';

import { ComplexCard } from '@/components/complex-card';
import { RegionSelector } from '@/components/region-selector';
import { SortSelector, type SortOrder } from '@/components/sort-selector';
import { useGetComplexes } from '@/hooks/use-complexes';

export function AptListPage() {
  // URL에 지역 선택 상태를 저장 (뒤로가기 시 복원됨)
  const [sidoCode, setSidoCode] = useQueryState('sido', { defaultValue: '' });
  const [sigunguCode, setSigunguCode] = useQueryState('sigungu', { defaultValue: '' });
  const [eupmyeondongCode, setEupmyeondongCode] = useQueryState('dong', { defaultValue: '' });
  const [sortOrder, setSortOrder] = useQueryState<SortOrder>('sortOrder', {
    defaultValue: 'DEFAULT',
  });

  const { data: complexesData, isLoading: isComplexesLoading } = useGetComplexes({
    sidoSigunguCode: sidoCode + sigunguCode,
    eupmyeondongCode: eupmyeondongCode || undefined,
    sortOrder,
  });

  const complexes = complexesData?.content ?? [];

  // 시도 변경 → 하위 선택 초기화
  const handleSidoChange = (code: string) => {
    setSidoCode(code);
    setSigunguCode('');
    setEupmyeondongCode('');
  };

  // 시군구 변경 → 읍면동 초기화
  const handleSigunguChange = (code: string) => {
    setSigunguCode(code);
    setEupmyeondongCode('');
  };

  return (
    <div className="min-h-screen bg-gray-50/50">
      {/* 상단 고정 헤더 */}
      <div className="sticky top-0 z-10 border-b bg-white/80 px-4 py-4 backdrop-blur-md">
        <div className="mx-auto max-w-6xl">
          <h1 className="mb-4 text-2xl font-bold tracking-tight text-gray-900">
            아파트 실거래가 조회
          </h1>

          <div className="flex flex-col gap-4">
            <RegionSelector
              sidoCode={sidoCode}
              sigunguCode={sigunguCode}
              eupmyeondongCode={eupmyeondongCode}
              onSidoChange={handleSidoChange}
              onSigunguChange={handleSigunguChange}
              onEupmyeondongChange={setEupmyeondongCode}
            />

            {/* 정렬 선택 */}
            {sigunguCode && (
              <SortSelector
                value={sortOrder}
                onChange={setSortOrder}
                disabled={isComplexesLoading}
              />
            )}
          </div>
        </div>
      </div>

      {/* 아파트 카드 리스트 */}
      <div className="px-4 py-8">
        <div className="mx-auto max-w-6xl">
          {!sigunguCode ? (
            <div className="flex flex-col items-center justify-center py-20 text-gray-400">
              <Search className="mb-4 h-12 w-12" />
              <p className="text-lg">시/도와 시/군/구를 선택해주세요</p>
            </div>
          ) : isComplexesLoading ? (
            <div className="flex items-center justify-center py-20">
              <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
            </div>
          ) : complexes.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-20 text-gray-400">
              <p className="text-lg">검색 결과가 없습니다</p>
            </div>
          ) : (
            <>
              <p className="mb-4 text-sm text-gray-500">
                총 {complexesData?.totalElements.toLocaleString()}개 단지
              </p>
              <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {complexes.map((complex) => (
                  <Link key={complex.complexId} href={`/${complex.complexId}`}>
                    <ComplexCard complex={complex} />
                  </Link>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

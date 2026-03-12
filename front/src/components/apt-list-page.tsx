'use client';

import { Loader2, Search } from 'lucide-react';
import Link from 'next/link';
import { useQueryState, parseAsStringEnum } from 'nuqs';

import { ComplexCard } from '@/components/complex-card';
import { RegionSelector } from '@/components/region-selector';
import { SortSelector, type SortOrder } from '@/components/sort-selector';
import { useGetComplexes } from '@/hooks/use-complexes';

export function AptListPage() {
    // URL에 지역 선택 상태를 저장 (뒤로가기 시 복원됨)
  const [sidoCode, setSidoCode] = useQueryState('sido', { defaultValue: '' });
    const [sigunguCode, setSigunguCode] = useQueryState('sigungu', { defaultValue: '' });
    const [eupmyeondongCode, setEupmyeondongCode] = useQueryState('dong', { defaultValue: '' });
    const [sortOrder, setSortOrder] = useQueryState('sortOrder', parseAsStringEnum<SortOrder>(['DEFAULT', 'TRADE_COUNT', 'BUILT_YEAR', 'POPULAR']).withDefault('DEFAULT'));

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
                                </h1>h1>
                      
                                <div className="flex flex-col gap-4">
                                            <RegionSelector
                                                            sidoCode={sidoCode}
                                                            sigunguCode={sigunguCode}
                                                            eupmyeondongCode={eupmyeondongCode}
                                                            onSidoChange={handleSidoChange}
                                                            onSigunguChange={handleSigunguChange}
                                                            onEupmyeondongChange={setEupmyeondongCode}
                                                          />
                                  {sidoCode && sigunguCode && (
                        <SortSelector
                                          value={sortOrder as SortOrder}
                                          onChange={(value) => setSortOrder(value)}
                                          disabled={isComplexesLoading}
                                        />
                      )}
                                </div>div>
                      </div>div>
              </div>div>
        
          {/* 콘텐츠 영역 */}
              <div className="mx-auto max-w-6xl px-4 py-6">
                {!sidoCode || !sigunguCode ? (
                    <div className="flex flex-col items-center justify-center py-20 text-gray-400">
                                <Search className="mb-4 h-12 w-12" />
                                <p className="text-lg font-medium">지역을 선택해주세요</p>p>
                                <p className="mt-1 text-sm">시/도와 시/군/구를 선택하면 아파트 목록이 표시됩니다</p>p>
                    </div>div>
                  ) : isComplexesLoading ? (
                    <div className="flex items-center justify-center py-20">
                                <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
                    </div>div>
                  ) : complexes.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-20 text-gray-400">
                                <p className="text-lg font-medium">검색 결과가 없습니다</p>p>
                    </div>div>
                  ) : (
                    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                      {complexes.map((complex) => (
                                    <Link
                                                      key={complex.complexId}
                                                      href={`/complex/${complex.complexId}`}
                                                      className="block transition-transform hover:scale-[1.02]"
                                                    >
                                                    <ComplexCard complex={complex} />
                                    </Link>Link>
                                  ))}
                    </div>div>
                      )}
              </div>div>
        </div>div>
      );
}</div>

'use client';

import { useEffect, useRef } from 'react';

import { Loader2, LogIn, LogOut, Search } from 'lucide-react';
import Link from 'next/link';
import { parseAsStringEnum, useQueryState } from 'nuqs';

import { ComplexCard } from '@/components/complex-card';
import { NotificationBell } from '@/components/notification-bell';
import { RegionSelector } from '@/components/region-selector';
import { SortSelector, type SortOrder } from '@/components/sort-selector';
import { useGetComplexes } from '@/hooks/use-complexes';
import { useAuth } from '@/lib/auth-context';

export function AptListPage() {
  // URL에 지역 선택 상태를 저장 (뒤로가기 시 복원됨)
  // history: push — 뒤로가기 시 지역 선택 상태 복원
  const [sidoCode, setSidoCode] = useQueryState('sido', { defaultValue: '', history: 'push' });
  const [sigunguCode, setSigunguCode] = useQueryState('sigungu', { defaultValue: '', history: 'push' });
  const [eupmyeondongCode, setEupmyeondongCode] = useQueryState('dong', { defaultValue: '', history: 'push' });
  const [sortOrder, setSortOrder] = useQueryState(
    'sortOrder',
    parseAsStringEnum<SortOrder>(['TRADE_COUNT']).withDefault('TRADE_COUNT').withOptions({ history: 'push' }),
  );

  const { user, logout } = useAuth();

  const {
    data,
    isLoading,
    isFetchingNextPage,
    fetchNextPage,
    hasNextPage,
  } = useGetComplexes({
    sidoSigunguCode: sidoCode + sigunguCode,
    eupmyeondongCode: eupmyeondongCode || undefined,
    sortOrder,
  });

  // 모든 페이지의 단지 목록 flatten
  const complexes = data?.pages.flatMap((page) => page.content) ?? [];
  const totalElements = data?.pages[0]?.totalElements ?? 0;

  // IntersectionObserver — 마지막 카드 진입 시 다음 페이지 fetch
  const sentinelRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { rootMargin: '200px' },
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

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
          <div className="mb-4 flex items-center justify-between">
            <h1 className="text-2xl font-bold tracking-tight text-gray-900">
              아파트 실거래가 조회
            </h1>
            {user ? (
              <div className="flex items-center gap-2">
                <NotificationBell />
                <span className="text-sm text-gray-600">{user.email}</span>
                <button
                  onClick={logout}
                  className="flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
                >
                  <LogOut className="h-4 w-4" />
                  로그아웃
                </button>
              </div>
            ) : (
              <Link
                href="/login"
                className="flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm text-blue-600 hover:bg-blue-50"
              >
                <LogIn className="h-4 w-4" />
                로그인
              </Link>
            )}
          </div>

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
                disabled={isLoading}
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
          ) : isLoading ? (
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
                총 {totalElements.toLocaleString()}개 단지
              </p>
              <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {complexes.map((complex) => (
                  <Link key={complex.complexId} href={`/${complex.complexId}`}>
                    <ComplexCard complex={complex} />
                  </Link>
                ))}
              </div>

              {/* 무한 스크롤 sentinel */}
              <div ref={sentinelRef} className="py-4">
                {isFetchingNextPage && (
                  <div className="flex justify-center">
                    <Loader2 className="h-6 w-6 animate-spin text-blue-400" />
                  </div>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

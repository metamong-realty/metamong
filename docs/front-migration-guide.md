# Metamong Frontend 마이그레이션 & 학습 가이드

> 이 문서는 새 Claude 세션에서 사용할 종합 가이드입니다.
> 기존 회사 프로젝트(wb-front)의 아파트 실거래가 기능을 metamong 사이드 프로젝트로 옮기면서, FE를 학습합니다.

---

## 목차

1. [프로젝트 컨텍스트](#1-프로젝트-컨텍스트)
2. [학습자 프로필 & 학습 방식](#2-학습자-프로필--학습-방식)
3. [현재 셋업 상태](#3-현재-셋업-상태)
4. [단계별 작업 계획](#4-단계별-작업-계획)
5. [마이그레이션 매핑 (사내 → 오픈소스)](#5-마이그레이션-매핑-사내--오픈소스)
6. [API 엔드포인트 목록](#6-api-엔드포인트-목록)
7. [원본 소스 코드 (참조용)](#7-원본-소스-코드-참조용)

---

## 1. 프로젝트 컨텍스트

### 뭘 만드는가

**아파트 실거래가 조회 서비스** — 시도/시군구/읍면동을 선택하면 아파트 단지 목록이 나오고, 단지를 클릭하면 매매/전세 거래 내역, 가격 추이 차트, 가격 비교 등을 볼 수 있는 서비스.

### 기존 구조 (wb-front 모노레포)

회사 프로젝트의 일부 기능으로 구현되어 있었음:
- 사내 디자인 시스템 (`@repo/5x`)
- 사내 API 클라이언트 (`createWebappFetchClient` + OpenAPI)
- `next-auth` 기반 인증
- `@lukemorales/query-key-factory` 3-layer 패턴

### 새 구조 (metamong)

```
metamong/
├── back/              # Kotlin Spring Boot (기존, 이미 운영 중)
│   ├── server/
│   ├── batch/
│   └── common/
├── front/             # Next.js (신규, 여기서 작업)
│   ├── src/
│   │   ├── app/
│   │   ├── components/ui/   # shadcn 7개 설치됨
│   │   └── lib/utils.ts
│   └── package.json
└── docs/
    └── front-migration-guide.md  # 이 문서
```

---

## 2. 학습자 프로필 & 학습 방식

### 학습자

- BE 개발자 (Kotlin, Spring Boot)
- FE는 몇 년 전 얕게 공부한 적 있음
- AI 사용해서 피쳐 작업은 가능한 수준
- 풀스택으로 확장하고 싶어서 대중적인 스택 위주로 배우려 함

### Claude가 도와주는 방식

**원칙: "코드 복붙이 아니라, 이해하면서 옮기기"**

각 단계마다 이 순서로 진행:

1. **코드 작성** — Claude가 기능 단위로 코드를 작성
2. **핵심 개념 설명** — 해당 코드에서 배워야 할 React/Next.js 개념을 설명
3. **질문 타임** — 이해 안 되는 부분 Q&A
4. **의도적 실험** — "이 코드에서 X를 빼면 어떻게 될까?" 같은 실험 제안
5. **다음 단계** — 이해되면 다음 기능으로

### 학습 포인트 체크리스트

매 단계에서 체크할 것들:

- [ ] React: `useState`, `useEffect`, `useMemo`, `props`
- [ ] React: 컴포넌트 분리와 합성
- [ ] React: 조건부 렌더링, 리스트 렌더링 (`map`)
- [ ] Next.js: App Router (layout, page, 동적 라우트)
- [ ] Next.js: `'use client'` vs Server Component
- [ ] Next.js: `Link`, `useSearchParams`, `useParams`
- [ ] TanStack Query: `useQuery`, `enabled`, `queryKey`
- [ ] TanStack Query: `useMutation`, `invalidateQueries`
- [ ] TypeScript: interface, union type, 제네릭
- [ ] Tailwind: 유틸리티 클래스, 반응형 (`sm:`, `md:`, `lg:`)
- [ ] shadcn/ui: 컴포넌트 사용법 (Select, Card, Tabs 등)
- [ ] Chart.js: 혼합 차트, 이중 Y축

---

## 3. 현재 셋업 상태

### 이미 완료된 것

- [x] `metamong/front/` 에 Next.js 프로젝트 생성 (`create-next-app`)
- [x] `.nvmrc` — Node 20 고정
- [x] 핵심 의존성 설치: `@tanstack/react-query`, `nuqs`, `chart.js`, `react-chartjs-2`, `lucide-react`
- [x] shadcn/ui 초기화 + 컴포넌트 7개: `select`, `card`, `badge`, `tabs`, `alert-dialog`, `button`, `sheet`
- [x] Tailwind CSS v4 설정

### 아직 안 한 것

- [ ] `QueryClientProvider` 설정 (`providers.tsx`)
- [ ] API 클라이언트 (`lib/api-client.ts`)
- [ ] 타입 정의 (`types/index.ts`)
- [ ] 유틸 함수 (`lib/format.ts`)
- [ ] 페이지 컴포넌트 (리스트, 상세)
- [ ] 차트 컴포넌트
- [ ] 커스텀 훅 (API 쿼리)
- [ ] 환경변수 설정 (`.env.local`)

### dev 서버 실행 방법

```bash
cd front
nvm use   # .nvmrc에서 Node 20 자동 적용
npm run dev
```

---

## 4. 단계별 작업 계획

### Step 1: 프로젝트 기반 셋업

**작업:** QueryClientProvider, API 클라이언트, 타입, 유틸 함수

**학습 포인트:**
- React Context와 Provider 패턴
- Next.js App Router에서 Provider 설정하는 방법
- TypeScript interface 정의

**생성할 파일:**
- `src/app/providers.tsx` — QueryClientProvider 래핑
- `src/lib/api-client.ts` — fetch 래퍼
- `src/lib/format.ts` — 가격/면적 포맷
- `src/types/index.ts` — 도메인 타입

### Step 2: 지역 선택 + 단지 목록 (리스트 페이지)

**작업:** 시도/시군구/읍면동 Select → 아파트 카드 리스트

**학습 포인트:**
- `useState`로 상태 관리
- `useQuery`로 데이터 fetching + `enabled` 조건부 호출
- Cascade 패턴: 시도 선택 → 시군구 로딩 → 읍면동 로딩
- `map`으로 리스트 렌더링
- shadcn `Select`, `Card` 사용법
- Tailwind Grid 레이아웃

**생성할 파일:**
- `src/hooks/use-regions.ts`
- `src/hooks/use-complexes.ts`
- `src/components/region-selector.tsx`
- `src/components/complex-card.tsx`
- `src/app/page.tsx` (리스트 페이지)

### Step 3: 상세 페이지 기본

**작업:** 동적 라우트 + 단지 정보 표시 + 평형 선택

**학습 포인트:**
- Next.js 동적 라우트 (`[complexId]/page.tsx`)
- `useParams`, `Link`를 이용한 페이지 이동
- `useEffect`의 올바른 사용 (초기값 설정)
- Props 전달과 컴포넌트 분리

**생성할 파일:**
- `src/app/[complexId]/page.tsx`
- `src/hooks/use-complex-detail.ts`
- `src/hooks/use-unit-types.ts`

### Step 4: 거래 내역 테이블

**작업:** 매매/전세 거래 목록 테이블 + 타입 필터 탭 + 기간 필터

**학습 포인트:**
- `useMemo`로 파생 데이터 계산
- `Tabs` 컴포넌트와 상태 연동
- 테이블 렌더링 패턴
- 조건부 스타일링 (Badge, 취소선 등)

**생성할 파일:**
- `src/hooks/use-trades.ts`
- `src/hooks/use-rents.ts`
- `src/components/transaction-table.tsx`

### Step 5: 차트

**작업:** 가격 추이 차트 (Bar + Line 혼합, 이중 Y축)

**학습 포인트:**
- Chart.js 등록 패턴 (`ChartJS.register`)
- 혼합 차트 구성 (Bar for 거래량, Line for 가격)
- `useMemo`로 차트 데이터 변환
- 커스텀 툴팁, 축 포맷터

**생성할 파일:**
- `src/hooks/use-charts.ts`
- `src/components/apt-price-chart.tsx`

### Step 6: 가격 비교 카드

**작업:** 최근 1개월 vs N개월 전 비교, 변동률

**학습 포인트:**
- `keepPreviousData` (이전 데이터 유지하며 로딩)
- 자식 컴포넌트에서 독립적인 상태 관리

**생성할 파일:**
- `src/hooks/use-price-summary.ts`
- `src/components/price-summary-card.tsx`

### Step 7: URL 상태 관리

**작업:** `nuqs`로 지역 선택을 URL에 반영 (뒤로가기 시 복원)

**학습 포인트:**
- `useQueryState` vs `useState`의 차이
- URL을 상태 저장소로 활용하는 패턴

### Step 8: 반응형 (모바일)

**작업:** 모바일에서는 Sheet(바텀시트)로 지역 선택

**학습 포인트:**
- Tailwind 반응형 클래스 (`hidden md:block`, `md:hidden`)
- shadcn `Sheet` 컴포넌트
- 단계별 모바일 UI 흐름

---

## 5. 마이그레이션 매핑 (사내 → 오픈소스)

### 컴포넌트 매핑

| 기존 (wb-front) | 신규 (metamong) |
|-----------------|-----------------|
| `@repo/5x` `<Button>` | shadcn `<Button>` from `@/components/ui/button` |
| `@repo/5x` `<BottomSheet>` | shadcn `<Sheet>` from `@/components/ui/sheet` |
| `@repo/5x` `cn()` | `cn()` from `@/lib/utils` (shadcn이 자동 생성) |
| `@repo/5x` `<IconCheck>` | `<Check>` from `lucide-react` |
| `<ClientDevice desktop>` | `<div className="hidden md:block">` |
| `<ClientDevice mobile>` | `<div className="md:hidden">` |

### API 클라이언트 매핑

기존 (3-layer 패턴, 파일 3개):
```
get-sido-list.ts          → API 함수 (createWebappFetchClient)
get-sido-list-queries.ts  → Query Key Factory (@lukemorales)
use-get-sido-list-query.ts → Hook (useSession + useQuery)
```

신규 (2-layer 패턴, 파일 1개):
```typescript
// hooks/use-regions.ts
import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '@/lib/api-client';

export const useGetSidoList = () =>
  useQuery({
    queryKey: ['regions', 'sido'],
    queryFn: () => apiFetch('/v1/apartments/regions/sido'),
  });
```

### API 클라이언트 구현

```typescript
// lib/api-client.ts
const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL;

export async function apiFetch<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!res.ok) {
    throw new Error(`API Error: ${res.status}`);
  }

  const json = await res.json();
  return json.data;
}
```

### Button variant 매핑

기존 `@repo/5x`:
```tsx
<Button variant="solid">  → <Button>                    // shadcn default
<Button variant="outline"> → <Button variant="outline">  // 동일
<Button variant="ghost">   → <Button variant="ghost">    // 동일
```

---

## 6. API 엔드포인트 목록

백엔드(metamong/back)가 제공하는 API:

| Method | Path | 설명 | Query Params |
|--------|------|------|-------------|
| GET | `/v1/apartments/regions/sido` | 시도 목록 | - |
| GET | `/v1/apartments/regions/sigungu` | 시군구 목록 | `sidoCode` |
| GET | `/v1/apartments/regions/eupmyeondong` | 읍면동 목록 | `sigunguCode` |
| GET | `/v1/apartments/complexes` | 단지 목록 | `sidoSigunguCode`, `eupmyeondongCode?`, `page`, `size` |
| GET | `/v1/apartments/complexes/{complexId}` | 단지 상세 | `unitTypeId?` |
| GET | `/v1/apartments/complexes/{complexId}/unit-types` | 평형 목록 | - |
| GET | `/v1/apartments/complexes/{complexId}/trades` | 매매 내역 | `unitTypeId?`, `period?`, `page`, `size` |
| GET | `/v1/apartments/complexes/{complexId}/trades/chart` | 매매 차트 | `unitTypeId?`, `period?` |
| GET | `/v1/apartments/complexes/{complexId}/rents` | 전세 내역 | `unitTypeId?`, `rentType?`, `period?`, `page`, `size` |
| GET | `/v1/apartments/complexes/{complexId}/rents/chart` | 전세 차트 | `unitTypeId?`, `rentType?`, `period?` |
| GET | `/v1/apartments/complexes/{complexId}/price-summary` | 가격 요약 | `unitTypeId?`, `lookbackMonths?` |
| POST | `/v1/apartments/complexes/{complexId}/subscriptions` | 알림 구독 | body: `{ unitTypeId? }` |
| DELETE | `/v1/apartments/subscriptions/{subscriptionId}` | 알림 해제 | - |

---

## 7. 원본 소스 코드 (참조용)

새 세션에서 wb-front에 접근할 필요 없도록, 원본 코드를 여기에 전부 포함합니다.

### 7-1. 리스트 페이지 (`apt-price-list-page.tsx`)

```tsx
'use client';

import { useMemo, useState } from 'react';

import Link from 'next/link';

import { Building2, ChevronRight, Loader2, MapPin, Search } from 'lucide-react';
import { useQueryState } from 'nuqs';

import { BottomSheet, Button, cn } from '@repo/5x';

import {
  useGetComplexesQuery,
  useGetEupmyeondongListQuery,
  useGetSidoListQuery,
  useGetSigunguListQuery,
} from '@/_entities/apartments';
import { ClientDevice } from '@/components/client-device';
import { Card, CardContent } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export function AptPriceListPage() {
  const [selectedSidoCode, setSelectedSidoCode] = useQueryState('sido', { defaultValue: '' });
  const [selectedSigunguCode, setSelectedSigunguCode] = useQueryState('sigungu', {
    defaultValue: '',
  });
  const [selectedEupmyeondongCode, setSelectedEupmyeondongCode] = useQueryState('dong', {
    defaultValue: '',
  });

  const [isBottomSheetOpen, setIsBottomSheetOpen] = useState(false);
  const [mobileStep, setMobileStep] = useState<'sido' | 'sigungu' | 'eupmyeondong'>('sido');

  const { data: sidoList = [], isLoading: isSidoLoading } = useGetSidoListQuery();
  const { data: sigunguList = [], isLoading: isSigunguLoading } =
    useGetSigunguListQuery(selectedSidoCode);
  const { data: eupmyeondongList = [], isLoading: isEupmyeondongLoading } =
    useGetEupmyeondongListQuery(selectedSigunguCode);

  const { data: complexesData, isLoading: isComplexesLoading } = useGetComplexesQuery({
    sidoSigunguCode: selectedSigunguCode,
    eupmyeondongCode: selectedEupmyeondongCode || undefined,
    pageable: { page: 0, size: 100 },
  });

  const complexes = complexesData?.content ?? [];

  const selectedSido = sidoList.find((s) => s.code === selectedSidoCode);
  const selectedSigungu = sigunguList.find((s) => s.code === selectedSigunguCode);
  const selectedEupmyeondong = eupmyeondongList.find((e) => e.code === selectedEupmyeondongCode);

  const handleSidoChange = (val: string) => {
    setSelectedSidoCode(val);
    setSelectedSigunguCode('');
    setSelectedEupmyeondongCode('');
  };

  const handleSigunguChange = (val: string) => {
    setSelectedSigunguCode(val);
    setSelectedEupmyeondongCode('');
  };

  const handleEupmyeondongChange = (val: string) => {
    setSelectedEupmyeondongCode(val);
  };

  const handleMobileSidoSelect = (code: string) => {
    setSelectedSidoCode(code);
    setSelectedSigunguCode('');
    setSelectedEupmyeondongCode('');
    setMobileStep('sigungu');
  };

  const handleMobileSigunguSelect = (code: string) => {
    setSelectedSigunguCode(code);
    setSelectedEupmyeondongCode('');
    setMobileStep('eupmyeondong');
  };

  const handleMobileEupmyeondongSelect = (code: string) => {
    setSelectedEupmyeondongCode(code);
    setIsBottomSheetOpen(false);
  };

  const openMobileFilter = () => {
    if (selectedSidoCode && !selectedSigunguCode) {
      setMobileStep('sigungu');
    } else if (selectedSigunguCode && !selectedEupmyeondongCode) {
      setMobileStep('eupmyeondong');
    } else {
      setMobileStep('sido');
    }
    setIsBottomSheetOpen(true);
  };

  const currentSelectionText = useMemo(() => {
    if (selectedSido && selectedSigungu && selectedEupmyeondong) {
      return `${selectedSido.name} ${selectedSigungu.name} ${selectedEupmyeondong.name}`;
    }
    if (selectedSido && selectedSigungu) {
      return `${selectedSido.name} ${selectedSigungu.name}`;
    }
    if (selectedSido) {
      return `${selectedSido.name}`;
    }
    return '지역을 선택해주세요';
  }, [selectedSido, selectedSigungu, selectedEupmyeondong]);

  const showComplexList = !!selectedSigunguCode;

  return (
    <div className="min-h-screen bg-gray-50/50">
      <div className="sticky top-0 z-10 border-b bg-white/80 px-4 py-4 backdrop-blur-md transition-all supports-[backdrop-filter]:bg-white/60">
        <div className="mx-auto max-w-6xl">
          <h1 className="mb-4 text-2xl font-bold tracking-tight text-gray-900">
            아파트 실거래가 조회
          </h1>

          {/* Desktop: 3-column Select */}
          <div className="hidden md:grid md:grid-cols-3 md:gap-3">
            <Select value={selectedSidoCode} onValueChange={handleSidoChange}>
              <SelectTrigger className="w-full bg-white transition-colors hover:bg-gray-50">
                <SelectValue placeholder={isSidoLoading ? '로딩중...' : '시/도 선택'} />
              </SelectTrigger>
              <SelectContent>
                {sidoList.map((sido) => (
                  <SelectItem key={sido.code} value={sido.code}>
                    {sido.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {/* ... 시군구, 읍면동 Select도 동일 패턴 */}
          </div>

          {/* Mobile: Button + Sheet */}
          <div className="md:hidden">
            <Button variant="outline" className="w-full" onClick={openMobileFilter}>
              {currentSelectionText}
            </Button>
            {/* Sheet로 BottomSheet 대체 */}
          </div>
        </div>
      </div>

      {/* 아파트 카드 리스트 */}
      <div className="px-4 py-8">
        <div className="mx-auto max-w-6xl">
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {complexes.map((complex) => (
              <Link key={complex.complexId} href={`/${complex.complexId}`}>
                <Card className="h-full hover:-translate-y-1 hover:shadow-lg transition-all">
                  <CardContent className="p-5">
                    {/* 단지명, 준공년도, 세대수, 위치, 거래량 */}
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
```

> 전체 코드(404줄)는 너무 길어 핵심 구조만 포함했습니다. 실제 옮길 때 Claude에게 "리스트 페이지 만들어줘"라고 하면 이 패턴을 기반으로 신규 코드를 작성합니다.

### 7-2. 상세 페이지 (`apt-price-detail-page.tsx`) — 핵심 구조

```tsx
'use client';

// 주요 import
import { useEffect, useMemo, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useSearchParams } from 'next/navigation';
import { ArrowLeft, Bell, BellOff, Check, Copy, ExternalLink, Loader2, TrendingDown, TrendingUp } from 'lucide-react';

// 타입 정의
type TransactionTypeFilter = '전체' | '매매' | '전세';
type TimePeriodFilter = 'RECENT_3YEARS' | 'ALL';
type LookbackMonthsFilter = 1 | 3 | 6 | 12 | 24 | 36 | 60;

// 유틸 함수
function formatPrice(price: number): string {
  const eok = Math.floor(price / 10000);
  const remainder = price % 10000;
  if (eok === 0) return `${remainder}만`;
  if (remainder === 0) return `${eok}억`;
  const cheonman = Math.floor(remainder / 1000);
  if (cheonman > 0) return `${eok}억 ${cheonman}천`;
  return `${eok}억 ${remainder}만`;
}

function formatSize(area: number, pyeong: number): string {
  return `${area}㎡ (${pyeong}평)`;
}

// 서브 컴포넌트: 주소 복사 버튼
function CopyButton({ text }: { text: string }) {
  const [copied, setCopied] = useState(false);
  const handleCopy = async () => {
    await navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };
  return (
    <button onClick={handleCopy} className="ml-2 rounded p-1 text-gray-400 hover:bg-gray-100">
      {copied ? <Check className="h-3.5 w-3.5 text-green-500" /> : <Copy className="h-3.5 w-3.5" />}
    </button>
  );
}

// 서브 컴포넌트: 가격 비교 카드
function PriceSummaryCard({ complexId, unitTypeId, transactionType }) {
  const [lookbackMonths, setLookbackMonths] = useState<LookbackMonthsFilter>(3);
  const { data: priceSummary } = useGetPriceSummaryQuery(complexId, { unitTypeId, lookbackMonths });
  // 최근 vs N개월 전 비교 + 변동률 표시
}

// 메인 컴포넌트
export function AptPriceDetailPage({ propertyId }: { propertyId: string }) {
  const complexId = parseInt(propertyId, 10);

  // 상태
  const [selectedUnitTypeId, setSelectedUnitTypeId] = useState<string>('');
  const [transactionType, setTransactionType] = useState<TransactionTypeFilter>('전체');
  const [timePeriod, setTimePeriod] = useState<TimePeriodFilter>('RECENT_3YEARS');
  const [showAllTransactions, setShowAllTransactions] = useState(false);

  // 데이터 쿼리
  const { data: complex, isLoading } = useGetComplexDetailQuery(complexId);
  const { data: unitTypes = [] } = useGetUnitTypesQuery(complexId);
  const { data: tradesData } = useGetTradesQuery(complexId, { unitTypeId, period, pageable });
  const { data: rentsData } = useGetRentsQuery(complexId, { unitTypeId, rentType, period, pageable });
  const { data: tradeChartData } = useGetTradeChartQuery(complexId, { unitTypeId, period });
  const { data: rentChartData } = useGetRentChartQuery(complexId, { unitTypeId, rentType, period });

  // 차트 데이터 변환 (useMemo)
  const chartData = useMemo(() => {
    // tradeChartData + rentChartData → ChartDataPoint[] 로 변환
  }, [tradeChartData, rentChartData]);

  // 거래 내역 합치기 + 필터링 (useMemo)
  const filteredTransactions = useMemo(() => {
    // trades + rents → 정렬 → transactionType 필터링
  }, [tradesData, rentsData, transactionType]);

  return (
    <div>
      {/* 헤더: 단지명, 주소, 평형 선택 */}
      {/* PriceSummaryCard */}
      {/* Tabs: 전체/매매/전세 */}
      {/*   차트 */}
      {/*   거래 내역 테이블 (30개 + 더보기) */}
      {/* 알림 구독 버튼 */}
      {/* 플로팅 버튼 (스크롤 시 표시/숨김) */}
      {/* AlertDialog */}
    </div>
  );
}
```

### 7-3. 차트 컴포넌트 (`apt-price-chart.tsx`) — 전체 코드

```tsx
'use client';

import { useMemo } from 'react';
import { Chart } from 'react-chartjs-2';
import {
  BarElement, CategoryScale, Chart as ChartJS, Legend,
  LineElement, LinearScale, PointElement, Title, Tooltip,
  type TooltipItem,
} from 'chart.js';

ChartJS.register(
  CategoryScale, LinearScale, PointElement,
  LineElement, BarElement, Title, Tooltip, Legend,
);

export interface ChartDataPoint {
  month: string;
  saleAvgPrice: number | null;
  leaseAvgPrice: number | null;
  saleCount: number;
  leaseCount: number;
}

export type TransactionTypeFilter = '전체' | '매매' | '전세';

interface AptPriceChartProps {
  chartData: ChartDataPoint[];
  transactionType: TransactionTypeFilter;
}

function formatPrice(price: number): string {
  const eok = Math.floor(price / 10000);
  const remainder = price % 10000;
  if (eok === 0) return `${remainder}만`;
  if (remainder === 0) return `${eok}억`;
  const cheonman = Math.floor(remainder / 1000);
  if (cheonman > 0) return `${eok}억 ${cheonman}천`;
  return `${eok}억 ${remainder}만`;
}

export function AptPriceChart({ chartData, transactionType }: AptPriceChartProps) {
  const { labels, datasets } = useMemo(() => {
    const labels = chartData.map((d) => d.month);
    const datasets = [];
    const showSale = transactionType === '전체' || transactionType === '매매';
    const showLease = transactionType === '전체' || transactionType === '전세';

    if (showSale) {
      datasets.push({
        type: 'bar' as const, label: '매매 거래량',
        data: chartData.map((d) => d.saleCount),
        backgroundColor: 'rgba(96, 165, 250, 0.5)', borderRadius: 4,
        yAxisID: 'y1', order: 2,
      });
    }
    if (showLease) {
      datasets.push({
        type: 'bar' as const, label: '전세 거래량',
        data: chartData.map((d) => d.leaseCount),
        backgroundColor: 'rgba(251, 146, 60, 0.5)', borderRadius: 4,
        yAxisID: 'y1', order: 2,
      });
    }
    if (showSale) {
      datasets.push({
        type: 'line' as const, label: '매매 가격',
        data: chartData.map((d) => d.saleAvgPrice),
        borderColor: '#2563eb', backgroundColor: '#2563eb',
        borderWidth: 3, pointRadius: 4, pointHoverRadius: 6,
        tension: 0.1, yAxisID: 'y', order: 1, spanGaps: true,
      });
    }
    if (showLease) {
      datasets.push({
        type: 'line' as const, label: '전세 가격',
        data: chartData.map((d) => d.leaseAvgPrice),
        borderColor: '#ea580c', backgroundColor: '#ea580c',
        borderWidth: 3, pointRadius: 4, pointHoverRadius: 6,
        tension: 0.1, yAxisID: 'y', order: 1, spanGaps: true,
      });
    }
    return { labels, datasets };
  }, [chartData, transactionType]);

  const options = useMemo(() => ({
    responsive: true, maintainAspectRatio: false,
    interaction: { mode: 'index' as const, intersect: false },
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(255,255,255,0.95)', titleColor: '#111827',
        bodyColor: '#374151', borderColor: '#e5e7eb', borderWidth: 1, padding: 12,
        callbacks: {
          label: (context: TooltipItem<'line' | 'bar'>) => {
            const label = context.dataset.label || '';
            const value = context.parsed.y;
            if (value === null) return '';
            return label.includes('가격') ? `${label}: ${formatPrice(value)}` : `${label}: ${value}건`;
          },
        },
      },
    },
    scales: {
      x: { grid: { display: false }, ticks: { maxRotation: 45, minRotation: 45, font: { size: 11 } } },
      y: {
        type: 'linear' as const, display: true, position: 'left' as const,
        title: { display: true, text: '가격', font: { size: 12 } },
        ticks: {
          callback: (value: number | string) => `${Math.round((typeof value === 'string' ? parseFloat(value) : value) / 10000)}억`,
          font: { size: 11 },
        },
        grid: { color: 'rgba(229,231,235,0.5)' },
      },
      y1: {
        type: 'linear' as const, display: true, position: 'right' as const,
        title: { display: true, text: '거래량', font: { size: 12 } },
        min: 0, suggestedMax: 10, ticks: { stepSize: 2, font: { size: 11 } },
        grid: { drawOnChartArea: false },
      },
    },
  }), []);

  if (chartData.length === 0) {
    return <div className="flex h-[300px] items-center justify-center text-gray-500">거래 내역 없음</div>;
  }

  const showSale = transactionType === '전체' || transactionType === '매매';
  const showLease = transactionType === '전체' || transactionType === '전세';

  return (
    <div className="space-y-4">
      {/* 커스텀 범례 */}
      <div className="flex flex-wrap items-center gap-4 text-sm">
        {showSale && (
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="h-0.5 w-6 bg-blue-600" /><span>매매 가격</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded-sm bg-blue-400 opacity-60" /><span>매매 거래량</span>
            </div>
          </div>
        )}
        {showLease && (
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="h-0.5 w-6 bg-orange-600" /><span>전세 가격</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded-sm bg-orange-400 opacity-60" /><span>전세 거래량</span>
            </div>
          </div>
        )}
      </div>
      <div className="h-[350px] w-full sm:h-[400px]">
        <Chart type="bar" data={{ labels, datasets }} options={options} />
      </div>
    </div>
  );
}
```

### 7-4. API 훅 패턴 (기존 3-layer → 신규 2-layer)

**기존 (wb-front) — 파일 3개 per endpoint:**

```typescript
// 1. get-sido-list.ts
export const getSidoList = async (accessToken: string) => {
  const res = await createWebappFetchClient(accessToken)['/v1/apartments/regions/sido'].GET();
  return res.data?.data;
};

// 2. get-sido-list-queries.ts
export const sidoListQueries = createQueryKeys('sidoList', {
  get: ({ accessToken }) => ({
    queryKey: ['sidoList'],
    queryFn: () => getSidoList(accessToken),
  }),
});

// 3. use-get-sido-list-query.ts
export const useGetSidoListQuery = () => {
  const { data: session } = useSession();
  return useQuery({
    ...sidoListQueries.get({ accessToken: session?.tokens.accessToken ?? '' }),
  });
};
```

**신규 (metamong) — 파일 1개 per domain:**

```typescript
// hooks/use-regions.ts
import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '@/lib/api-client';
import type { Region } from '@/types';

export const useGetSidoList = () =>
  useQuery({
    queryKey: ['regions', 'sido'],
    queryFn: () => apiFetch<Region[]>('/v1/apartments/regions/sido'),
  });

export const useGetSigunguList = (sidoCode: string) =>
  useQuery({
    queryKey: ['regions', 'sigungu', sidoCode],
    queryFn: () => apiFetch<Region[]>(`/v1/apartments/regions/sigungu?sidoCode=${sidoCode}`),
    enabled: !!sidoCode,
  });

export const useGetEupmyeondongList = (sigunguCode: string) =>
  useQuery({
    queryKey: ['regions', 'eupmyeondong', sigunguCode],
    queryFn: () => apiFetch<Region[]>(`/v1/apartments/regions/eupmyeondong?sigunguCode=${sigunguCode}`),
    enabled: !!sigunguCode,
  });
```

### 7-5. 타입 정의

```typescript
// types/index.ts

export interface Region {
  name: string;
  code: string;
}

export interface Complex {
  complexId: number;
  name: string;
  builtYear: number;
  addressShort?: string;
  addressRoad?: string;
  addressJibun?: string;
  eupmyeondongName?: string;
  totalHousehold?: number;
  totalBuilding?: number;
  heatingType?: string;
  floorAreaRatio?: number;
  buildingCoverageRatio?: number;
  totalTradeCount?: number;
  recent3YearsTradeCount?: number;
  isSubscribed?: boolean;
  subscriptionId?: number;
}

export interface UnitType {
  unitTypeId: number;
  exclusiveArea: number;
  exclusivePyeong: number;
  supplyArea?: number;
}

export interface Trade {
  tradeId: number;
  contractDate: string;
  price: number;
  exclusiveArea: number;
  exclusivePyeong: number;
  floor: number;
  isDirect: boolean;
  isCanceled: boolean;
}

export interface Rent {
  rentId: number;
  contractDate: string;
  deposit: number;
  exclusiveArea: number;
  exclusivePyeong: number;
  floor: number;
  isCanceled: boolean;
}

export interface ChartDataPoint {
  month: string;
  saleAvgPrice: number | null;
  leaseAvgPrice: number | null;
  saleCount: number;
  leaseCount: number;
}

export interface PriceSummary {
  trade?: {
    recentMonthAvgPrice: number;
    lookbackMonthAvgPrice?: number;
    priceChangeRate?: number;
  };
  rent?: {
    recentMonthAvgDeposit: number;
    lookbackMonthAvgDeposit?: number;
    depositChangeRate?: number;
  };
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
}

export type TransactionTypeFilter = '전체' | '매매' | '전세';
export type TimePeriodFilter = 'RECENT_3YEARS' | 'ALL';
export type LookbackMonths = 1 | 3 | 6 | 12 | 24 | 36 | 60;
```

### 7-6. 유틸 함수

```typescript
// lib/format.ts

export function formatPrice(price: number): string {
  const eok = Math.floor(price / 10000);
  const remainder = price % 10000;

  if (eok === 0) return `${remainder}만`;
  if (remainder === 0) return `${eok}억`;

  const cheonman = Math.floor(remainder / 1000);
  if (cheonman > 0) return `${eok}억 ${cheonman}천`;

  return `${eok}억 ${remainder}만`;
}

export function formatSize(area: number, pyeong: number): string {
  return `${area}㎡ (${pyeong}평)`;
}
```

---

## 새 세션에서 시작할 때

```
metamong/ 디렉토리에서 Claude 세션을 열고:

"docs/front-migration-guide.md 읽고, Step 1부터 시작하자.
front/ 디렉토리에 코드 작성해줘. 각 단계마다 핵심 개념 설명도 같이."
```

이렇게 말하면 됩니다.

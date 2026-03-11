---
globs: "front/src/app/**/*.tsx,front/src/app/**/*.ts"
---

# Next.js App Router Patterns

## Server vs Client Component

- **기본은 Server Component** — `'use client'` 없으면 서버
- **`'use client'` 필요한 경우**: `useState`, `useEffect`, `onClick`, 브라우저 API 사용 시
- Client 경계를 최대한 아래로 내리기 (leaf에서 선언)

```tsx
// app/[complexId]/page.tsx — Server Component (데이터 없이 렌더)
export default function ComplexDetailPage({ params }: { params: { complexId: string } }) {
  return <ComplexDetail complexId={params.complexId} />;
}

// components/complex-detail.tsx — Client Component (상태/훅 사용)
'use client';
export function ComplexDetail({ complexId }: { complexId: string }) {
  const [tab, setTab] = useState('전체');
  // ...
}
```

## Layout & Page 규칙

- `layout.tsx`: 공유 UI (헤더, Provider), 네비게이션 시 리렌더 안 됨
- `page.tsx`: 라우트 진입점, URL params 받기
- 동적 라우트: `[complexId]/page.tsx`

```
front/src/app/
├── layout.tsx              # Root layout (Provider, 폰트)
├── page.tsx                # 홈 (리스트 페이지)
├── [complexId]/
│   └── page.tsx            # 상세 페이지
└── globals.css
```

## Provider 설정

- `QueryClientProvider`는 별도 Client Component로 분리
- Root Layout에서 감싸기

```tsx
// app/providers.tsx
'use client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState } from 'react';

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: { queries: { staleTime: 60 * 1000 } },
  }));
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
```

## 데이터 페칭 패턴

- API 호출은 커스텀 훅으로 (`hooks/use-*.ts`)
- `useQuery`의 `enabled` 옵션으로 조건부 호출
- queryKey 계층: `['도메인', '리소스', params]`

```typescript
// hooks/use-regions.ts
export const useGetSigunguList = (sidoCode: string) =>
  useQuery({
    queryKey: ['regions', 'sigungu', sidoCode],
    queryFn: () => apiFetch<Region[]>(`/v1/apartments/regions/sigungu?sidoCode=${sidoCode}`),
    enabled: !!sidoCode,
  });
```

## 네비게이션

- 페이지 이동: `<Link href={...}>` (Next.js)
- URL 상태: `nuqs`의 `useQueryState`
- 동적 파라미터: `useParams()`

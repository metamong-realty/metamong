---
globs: "front/**/*.tsx,front/**/*.ts"
---

# Frontend Style Guidelines

## Naming Rules

| 대상 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트 | PascalCase | `RegionSelector`, `ComplexCard` |
| 훅 | camelCase, `use` 접두사 | `useGetSidoList`, `useRegions` |
| 유틸 함수 | camelCase | `formatPrice`, `formatSize` |
| 타입/인터페이스 | PascalCase | `Region`, `Complex`, `Trade` |
| 상수 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| 파일명 (컴포넌트) | kebab-case | `region-selector.tsx` |
| 파일명 (훅) | kebab-case, `use-` 접두사 | `use-regions.ts` |
| 파일명 (타입) | kebab-case | `index.ts` in `types/` |

## TypeScript

- `any` 사용 금지 — `unknown`이나 구체적 타입 사용
- `as` 타입 단언 최소화 — 타입 가드 우선
- API 응답은 반드시 타입 정의 (`types/index.ts`)
- `interface` 우선 (확장 필요 시), 유니온은 `type`

```typescript
// Good
interface ComplexCardProps {
  complex: Complex;
  onClick?: () => void;
}

// Bad
const ComplexCard = (props: any) => { ... }
```

## Import 순서

1. React / Next.js
2. 외부 라이브러리 (`@tanstack`, `lucide-react`, `nuqs`)
3. 내부 컴포넌트 (`@/components/`)
4. 훅 (`@/hooks/`)
5. 유틸/타입 (`@/lib/`, `@/types/`)

```typescript
'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';

import { useQuery } from '@tanstack/react-query';
import { Building2, ChevronRight } from 'lucide-react';

import { Card, CardContent } from '@/components/ui/card';
import { useGetSidoList } from '@/hooks/use-regions';
import { formatPrice } from '@/lib/format';
import type { Complex } from '@/types';
```

## Tailwind CSS

- 인라인 스타일 금지 — Tailwind 유틸리티 클래스 사용
- 반응형: `sm:` → `md:` → `lg:` (모바일 퍼스트)
- 다크모드: 현재 미적용 (추후 `dark:` 접두사)
- 클래스 병합: `cn()` 유틸리티 사용

```tsx
// Good
<div className={cn('flex items-center gap-2', isActive && 'text-blue-600')}>

// Bad
<div style={{ display: 'flex', alignItems: 'center' }}>
```

## 함수 컴포넌트

- `function` 선언 또는 `const` + 화살표 함수 일관성 유지
- Props는 구조 분해 할당
- 불필요한 `React.FC` 사용 금지

```tsx
// Good
export function RegionSelector({ onSelect }: RegionSelectorProps) { ... }

// Also Good
export const RegionSelector = ({ onSelect }: RegionSelectorProps) => { ... }
```

## 상태 관리

- 서버 상태: TanStack Query (`useQuery`, `useMutation`)
- URL 상태: `nuqs` (`useQueryState`)
- 로컬 UI 상태: `useState`
- 파생 데이터: `useMemo` (불필요한 상태 생성 금지)

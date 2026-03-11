---
globs: "front/src/components/**/*.tsx"
---

# Component Design Guidelines

## shadcn/ui 사용 원칙

- UI 프리미티브는 반드시 `@/components/ui/` 것 사용
- 직접 HTML로 Select, Dialog 등 만들지 않기
- 아이콘: `lucide-react` 사용

```tsx
// Good
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent } from '@/components/ui/card';
import { Building2 } from 'lucide-react';

// Bad — shadcn에 있는 걸 직접 구현
<select className="border rounded p-2">...</select>
```

## 설치된 shadcn 컴포넌트

`select`, `card`, `badge`, `tabs`, `alert-dialog`, `button`, `sheet`

추가 필요 시: `npx shadcn@latest add [컴포넌트명]`

## 컴포넌트 파일 구조

```
src/components/
├── ui/                    # shadcn 컴포넌트 (수정 최소화)
│   ├── button.tsx
│   ├── card.tsx
│   └── ...
├── region-selector.tsx    # 비즈니스 컴포넌트
├── complex-card.tsx
├── transaction-table.tsx
└── apt-price-chart.tsx
```

## Props 설계

- 필수 props는 `required`, 선택은 `?`
- 콜백은 `on` 접두사: `onChange`, `onSelect`
- children 패턴 활용 (합성)

```tsx
interface RegionSelectorProps {
  selectedSidoCode: string;
  onSidoChange: (code: string) => void;
  isLoading?: boolean;
}
```

## 조건부 렌더링

```tsx
// 간단한 조건: &&
{isLoading && <Loader2 className="animate-spin" />}

// 양자택일: 삼항
{complexes.length > 0 ? <ComplexList /> : <EmptyState />}

// 복잡한 조건: early return
if (isLoading) return <Skeleton />;
if (error) return <ErrorMessage />;
return <Content />;
```

## 리스트 렌더링

- `key`는 반드시 고유한 ID 사용 (index 금지)

```tsx
{complexes.map((complex) => (
  <ComplexCard key={complex.complexId} complex={complex} />
))}
```

## 반응형 패턴

- 데스크톱: `hidden md:block` (md 이상에서 표시)
- 모바일: `md:hidden` (md 미만에서 표시)
- 모바일 바텀시트: shadcn `Sheet` (side="bottom")

```tsx
{/* 데스크톱: Select 3개 */}
<div className="hidden md:grid md:grid-cols-3 md:gap-3">
  <Select>...</Select>
</div>

{/* 모바일: Sheet로 단계별 선택 */}
<div className="md:hidden">
  <Sheet>...</Sheet>
</div>
```

## className 병합

조건부 클래스는 `cn()` 유틸리티 사용:

```tsx
import { cn } from '@/lib/utils';

<Card className={cn(
  'h-full transition-all',
  isSelected && 'ring-2 ring-blue-500',
)}>
```

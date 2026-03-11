'use client';

import { useState } from 'react';

import { ArrowDown, ArrowUp, Minus, TrendingUp } from 'lucide-react';

import { Card, CardContent } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useGetPriceSummary } from '@/hooks/use-price-summary';
import { formatPrice } from '@/lib/format';
import { cn } from '@/lib/utils';
import type { LookbackMonths, TransactionTypeFilter } from '@/types';

const LOOKBACK_OPTIONS: { value: LookbackMonths; label: string }[] = [
  { value: 1, label: '1개월 전' },
  { value: 3, label: '3개월 전' },
  { value: 6, label: '6개월 전' },
  { value: 12, label: '1년 전' },
  { value: 24, label: '2년 전' },
  { value: 36, label: '3년 전' },
  { value: 60, label: '5년 전' },
];

interface PriceSummaryCardProps {
  complexId: number;
  unitTypeId?: number;
  transactionType: TransactionTypeFilter;
}

export function PriceSummaryCard({
  complexId,
  unitTypeId,
  transactionType,
}: PriceSummaryCardProps) {
  const [lookbackMonths, setLookbackMonths] = useState<LookbackMonths>(3);

  const { data: priceSummary, isPlaceholderData } = useGetPriceSummary(complexId, {
    unitTypeId,
    lookbackMonths,
  });

  const showTrade = transactionType === '전체' || transactionType === '매매';
  const showRent = transactionType === '전체' || transactionType === '전세';

  const lookbackLabel = LOOKBACK_OPTIONS.find((o) => o.value === lookbackMonths)?.label ?? '';

  return (
    <Card className={cn(isPlaceholderData && 'opacity-70 transition-opacity')}>
      <CardContent className="p-5">
        {/* 헤더 */}
        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-blue-600" />
            <h3 className="text-sm font-semibold text-gray-900">가격 비교</h3>
          </div>
          <Select
            value={String(lookbackMonths)}
            onValueChange={(v) => setLookbackMonths(Number(v) as LookbackMonths)}
          >
            <SelectTrigger className="h-8 w-[120px] text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {LOOKBACK_OPTIONS.map((opt) => (
                <SelectItem key={opt.value} value={String(opt.value)}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* 비교 카드들 */}
        <div className={cn('grid gap-3', showTrade && showRent ? 'grid-cols-2' : 'grid-cols-1')}>
          {showTrade && (
            <PriceCompareItem
              label="매매"
              recentPrice={priceSummary?.trade?.recentMonthAvgPrice ?? null}
              lookbackPrice={priceSummary?.trade?.lookbackMonthAvgPrice ?? null}
              changeRate={priceSummary?.trade?.priceChangeRate ?? null}
              lookbackLabel={lookbackLabel}
              accentColor="blue"
            />
          )}
          {showRent && (
            <PriceCompareItem
              label="전세"
              recentPrice={priceSummary?.rent?.recentMonthAvgDeposit ?? null}
              lookbackPrice={priceSummary?.rent?.lookbackMonthAvgDeposit ?? null}
              changeRate={priceSummary?.rent?.depositChangeRate ?? null}
              lookbackLabel={lookbackLabel}
              accentColor="emerald"
            />
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function PriceCompareItem({
  label,
  recentPrice,
  lookbackPrice,
  changeRate,
  lookbackLabel,
  accentColor,
}: {
  label: string;
  recentPrice: number | null;
  lookbackPrice: number | null;
  changeRate: number | null;
  lookbackLabel: string;
  accentColor: 'blue' | 'emerald';
}) {
  const hasData = recentPrice !== null;

  if (!hasData) {
    return (
      <div className="rounded-lg bg-gray-50 p-4">
        <p className="mb-2 text-xs font-medium text-gray-500">{label}</p>
        <p className="text-sm text-gray-400">거래 데이터 없음</p>
      </div>
    );
  }

  const isUp = changeRate !== null && changeRate > 0;
  const isDown = changeRate !== null && changeRate < 0;

  return (
    <div className="rounded-lg bg-gray-50 p-4">
      <p className="mb-2 text-xs font-medium text-gray-500">{label}</p>

      {/* 최근 평균가 */}
      <p
        className={cn(
          'text-lg font-bold',
          accentColor === 'blue' ? 'text-blue-600' : 'text-emerald-600',
        )}
      >
        {formatPrice(recentPrice)}
      </p>
      <p className="mb-3 text-xs text-gray-400">최근 1개월 평균</p>

      {/* 비교 가격 */}
      {lookbackPrice !== null && (
        <div className="border-t border-gray-200 pt-3">
          <p className="text-sm text-gray-600">{formatPrice(lookbackPrice)}</p>
          <p className="text-xs text-gray-400">{lookbackLabel} 평균</p>
        </div>
      )}

      {/* 변동률 */}
      {changeRate !== null && (
        <div
          className={cn(
            'mt-2 inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium',
            isUp && 'bg-red-50 text-red-600',
            isDown && 'bg-blue-50 text-blue-600',
            !isUp && !isDown && 'bg-gray-100 text-gray-500',
          )}
        >
          {isUp && <ArrowUp className="h-3 w-3" />}
          {isDown && <ArrowDown className="h-3 w-3" />}
          {!isUp && !isDown && <Minus className="h-3 w-3" />}
          {changeRate > 0 ? '+' : ''}
          {changeRate.toFixed(1)}%
        </div>
      )}
    </div>
  );
}

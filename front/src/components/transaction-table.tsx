'use client';

import { useMemo, useState } from 'react';

import { ChevronDown } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { formatPrice } from '@/lib/format';
import type { Rent, Trade, TransactionTypeFilter } from '@/types';

// 매매와 전세를 하나의 행으로 합치기 위한 통합 타입
interface TransactionRow {
  id: string;
  type: '매매' | '전세' | '월세';
  contractDate: string;
  exclusivePyeong: number | null;
  floor: number | null;
  price: string;
  isCanceled: boolean;
  isDirect?: boolean;
}

interface TransactionTableProps {
  trades: Trade[];
  rents: Rent[];
  transactionType: TransactionTypeFilter;
}

const INITIAL_DISPLAY_COUNT = 30;

export function TransactionTable({ trades, rents, transactionType }: TransactionTableProps) {
  const [showAll, setShowAll] = useState(false);

  // 매매 + 전세를 하나의 리스트로 합치고, 필터링 + 날짜순 정렬
  const rows = useMemo(() => {
    const tradeRows: TransactionRow[] =
      transactionType !== '전세'
        ? trades.map((t) => ({
            id: `trade-${t.tradeId}`,
            type: '매매' as const,
            contractDate: t.contractDate,
            exclusivePyeong: t.exclusivePyeong,
            floor: t.floor,
            price: formatPrice(t.price),
            isCanceled: t.isCanceled,
            isDirect: t.isDirect,
          }))
        : [];

    const rentRows: TransactionRow[] =
      transactionType !== '매매'
        ? rents.map((r) => ({
            id: `rent-${r.rentId}`,
            type: r.rentType === 'JEONSE' ? ('전세' as const) : ('월세' as const),
            contractDate: r.contractDate,
            exclusivePyeong: r.exclusivePyeong,
            floor: r.floor,
            price:
              r.rentType === 'JEONSE'
                ? formatPrice(r.deposit)
                : `${formatPrice(r.deposit)} / ${r.monthlyRent}만`,
            isCanceled: r.isCanceled,
          }))
        : [];

    return [...tradeRows, ...rentRows].sort(
      (a, b) => new Date(b.contractDate).getTime() - new Date(a.contractDate).getTime(),
    );
  }, [trades, rents, transactionType]);

  const displayedRows = showAll ? rows : rows.slice(0, INITIAL_DISPLAY_COUNT);
  const hasMore = rows.length > INITIAL_DISPLAY_COUNT;

  if (rows.length === 0) {
    return (
      <div className="flex items-center justify-center py-12 text-sm text-gray-400">
        거래 내역이 없습니다
      </div>
    );
  }

  return (
    <div>
      {/* 테이블 헤더 */}
      <div className="grid grid-cols-[1fr_70px_50px_40px_100px] gap-2 border-b px-3 py-2 text-xs font-medium text-gray-500">
        <span>계약일</span>
        <span>유형</span>
        <span className="text-center">평형</span>
        <span className="text-center">층</span>
        <span className="text-right">가격</span>
      </div>

      {/* 테이블 바디 */}
      <div className="divide-y">
        {displayedRows.map((row) => (
          <div
            key={row.id}
            className={`grid grid-cols-[1fr_70px_50px_40px_100px] items-center gap-2 px-3 py-2.5 text-sm ${
              row.isCanceled ? 'text-gray-300' : 'text-gray-700'
            }`}
          >
            <span className={row.isCanceled ? 'line-through' : ''}>{row.contractDate}</span>

            <span>
              <TypeBadge type={row.type} isCanceled={row.isCanceled} />
              {row.isDirect && (
                <Badge variant="outline" className="ml-1 text-[10px]">
                  직
                </Badge>
              )}
            </span>

            <span className="text-center">
              {row.exclusivePyeong ? `${row.exclusivePyeong}평` : '-'}
            </span>

            <span className="text-center">{row.floor ? `${row.floor}층` : '-'}</span>

            <span className={`text-right font-medium ${row.isCanceled ? 'line-through' : ''}`}>
              {row.price}
            </span>
          </div>
        ))}
      </div>

      {/* 더보기 버튼 */}
      {hasMore && !showAll && (
        <button
          onClick={() => setShowAll(true)}
          className="flex w-full items-center justify-center gap-1 border-t py-3 text-sm text-gray-500 hover:bg-gray-50"
        >
          <ChevronDown className="h-4 w-4" />
          전체 {rows.length}건 보기
        </button>
      )}
    </div>
  );
}

function TypeBadge({ type, isCanceled }: { type: string; isCanceled: boolean }) {
  const colorMap: Record<string, string> = {
    매매: isCanceled ? 'bg-gray-100 text-gray-400' : 'bg-blue-50 text-blue-700',
    전세: isCanceled ? 'bg-gray-100 text-gray-400' : 'bg-orange-50 text-orange-700',
    월세: isCanceled ? 'bg-gray-100 text-gray-400' : 'bg-green-50 text-green-700',
  };

  return (
    <Badge variant="secondary" className={`text-[10px] ${colorMap[type] ?? ''}`}>
      {isCanceled ? `${type}(취소)` : type}
    </Badge>
  );
}

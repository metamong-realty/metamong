'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';

import {
  ArrowLeft,
  Building2,
  Check,
  Copy,
  Loader2,
  MapPin,
} from 'lucide-react';

import { AddressLink } from '@/components/address-link';
import { ApartmentSpecs } from '@/components/apartment-specs';
import { AptPriceChart, type ChartDataPoint } from '@/components/apt-price-chart';
import { PriceSummaryCard } from '@/components/price-summary-card';
import { TransactionTable } from '@/components/transaction-table';
import { Card, CardContent } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useGetTradeChart, useGetRentChart } from '@/hooks/use-charts';
import { useGetComplexDetail } from '@/hooks/use-complex-detail';
import { useGetRents } from '@/hooks/use-rents';
import { useGetTrades } from '@/hooks/use-trades';
import { useGetUnitTypes } from '@/hooks/use-unit-types';
import type { TimePeriodFilter, TransactionTypeFilter } from '@/types';

interface ComplexDetailProps {
  complexId: string;
}

function CopyButton({ text }: { text: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    await navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <button onClick={handleCopy} className="ml-1 rounded p-1 text-gray-400 hover:bg-gray-100">
      {copied ? <Check className="h-3.5 w-3.5 text-green-500" /> : <Copy className="h-3.5 w-3.5" />}
    </button>
  );
}

export function ComplexDetail({ complexId }: ComplexDetailProps) {
  const numericId = parseInt(complexId, 10);

  const { data: complex, isLoading } = useGetComplexDetail(numericId);
  const { data: unitTypes = [] } = useGetUnitTypes(numericId);

  const [manualUnitTypeId, setManualUnitTypeId] = useState<string>('');
  const [transactionType, setTransactionType] = useState<TransactionTypeFilter>('전체');
  const [period, setPeriod] = useState<TimePeriodFilter>('RECENT_3YEARS');

  // 사용자가 직접 선택했으면 그 값, 아니면 첫 번째 평형을 기본값으로
  const selectedUnitTypeId =
    manualUnitTypeId || (unitTypes.length > 0 ? String(unitTypes[0].unitTypeId) : '');

  const unitTypeIdNum = selectedUnitTypeId ? parseInt(selectedUnitTypeId, 10) : undefined;
  const { data: tradesData, isLoading: isTradesLoading } = useGetTrades(numericId, { unitTypeId: unitTypeIdNum, period });
  const { data: rentsData, isLoading: isRentsLoading } = useGetRents(numericId, { unitTypeId: unitTypeIdNum, period });
  const { data: tradeChartData } = useGetTradeChart(numericId, { unitTypeId: unitTypeIdNum, period });
  const { data: rentChartData } = useGetRentChart(numericId, { unitTypeId: unitTypeIdNum, period });

  // 매매 + 전세 차트 데이터를 하나의 ChartDataPoint[] 로 합치기
  const chartData = useMemo(() => {
    const tradePrice = tradeChartData?.priceChart ?? [];
    const tradeVolume = tradeChartData?.volumeChart ?? [];
    const rentPrice = rentChartData?.priceChart ?? [];
    const rentVolume = rentChartData?.volumeChart ?? [];

    // 모든 월을 합집합으로 모은 뒤 정렬
    const allMonths = new Set([
      ...tradePrice.map((d) => d.yearMonth),
      ...rentPrice.map((d) => d.yearMonth),
    ]);
    const sortedMonths = [...allMonths].sort();

    const tradePriceMap = new Map(tradePrice.map((d) => [d.yearMonth, d]));
    const tradeVolumeMap = new Map(tradeVolume.map((d) => [d.yearMonth, d]));
    const rentPriceMap = new Map(rentPrice.map((d) => [d.yearMonth, d]));
    const rentVolumeMap = new Map(rentVolume.map((d) => [d.yearMonth, d]));

    return sortedMonths.map(
      (month): ChartDataPoint => ({
        month,
        saleAvgPrice: tradePriceMap.get(month)?.avgPrice ?? null,
        leaseAvgPrice: rentPriceMap.get(month)?.avgDeposit ?? null,
        saleCount: tradeVolumeMap.get(month)?.tradeCount ?? 0,
        leaseCount: rentVolumeMap.get(month)?.rentCount ?? 0,
      }),
    );
  }, [tradeChartData, rentChartData]);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
      </div>
    );
  }

  if (!complex) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 text-gray-400">
        <p className="text-lg">단지 정보를 찾을 수 없습니다</p>
        <Link href="/" className="text-blue-600 hover:underline">
          목록으로 돌아가기
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50/50">
      {/* 상단 헤더 */}
      <div className="sticky top-0 z-10 border-b bg-white/80 px-4 py-4 backdrop-blur-md">
        <div className="mx-auto max-w-4xl">
          <div className="flex items-center gap-3">
            <Link href="/" className="rounded-lg p-1.5 hover:bg-gray-100">
              <ArrowLeft className="h-5 w-5 text-gray-600" />
            </Link>
            <div className="flex items-center gap-2">
              <Building2 className="h-5 w-5 text-blue-600" />
              <h1 className="text-xl font-bold text-gray-900">{complex.name}</h1>
            </div>
          </div>
        </div>
      </div>

      <div className="px-4 py-6">
        <div className="mx-auto max-w-4xl space-y-6">
          {/* 주소 카드 */}
          <Card>
            <CardContent className="p-5">
              {/* 주소 */}
              {(complex.addressRoad || complex.addressJibun) && (
                <div className="space-y-1 rounded-lg border border-gray-200 bg-gray-50 p-3">
                  <div className="mb-2 flex items-center gap-1.5">
                    <MapPin className="h-4 w-4 text-gray-500" />
                    <span className="text-xs font-semibold text-gray-700">주소</span>
                  </div>
                  {complex.addressRoad && (
                    <AddressLink address={complex.addressRoad} type="도로명" />
                  )}
                  {complex.addressJibun && (
                    <AddressLink address={complex.addressJibun} type="지번" />
                  )}
                </div>
              )}
            </CardContent>
          </Card>

          {/* 단지 정보 (ApartmentSpecs 컴포넌트 사용) */}
          <ApartmentSpecs
            builtYear={complex.builtYear}
            totalHousehold={complex.totalHousehold}
            totalBuilding={complex.totalBuilding}
            totalParking={complex.totalParking}
            floorAreaRatio={complex.floorAreaRatio}
            buildingCoverageRatio={complex.buildingCoverageRatio}
            heatingType={complex.heatingType}
          />

          {/* 필터 선택 */}
          <div className="flex flex-wrap items-center gap-6">
            {/* 평형 선택 */}
            {unitTypes.length > 0 && (
              <div className="flex items-center gap-3">
                <span className="text-sm font-medium text-gray-700">평형 선택</span>
                <Select value={selectedUnitTypeId} onValueChange={setManualUnitTypeId}>
                  <SelectTrigger className="w-[140px]">
                    <SelectValue placeholder="평형" />
                  </SelectTrigger>
                  <SelectContent>
                    {unitTypes.map((ut) => (
                      <SelectItem key={ut.unitTypeId} value={String(ut.unitTypeId)}>
                        {ut.exclusivePyeong}평
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            {/* 조회 기간 선택 */}
            <div className="flex items-center gap-3">
              <span className="text-sm font-medium text-gray-700">조회 기간</span>
              <Select value={period} onValueChange={(v) => setPeriod(v as TimePeriodFilter)}>
                <SelectTrigger className="w-[140px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="RECENT_3YEARS">최근 3년</SelectItem>
                  <SelectItem value="ALL">전체</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* 거래 내역 */}
          <Card>
            <CardContent className="p-0">
              <Tabs
                value={transactionType}
                onValueChange={(v) => setTransactionType(v as TransactionTypeFilter)}
              >
                <div className="border-b px-4 pt-4">
                  <TabsList>
                    <TabsTrigger value="전체">전체</TabsTrigger>
                    <TabsTrigger value="매매">매매</TabsTrigger>
                    <TabsTrigger value="전세">전세</TabsTrigger>
                  </TabsList>
                </div>

                <TabsContent value={transactionType} className="mt-0 space-y-4">
                  {/* 차트 */}
                  <div className="px-4 pt-4">
                    <AptPriceChart chartData={chartData} transactionType={transactionType} />
                  </div>

                  {/* 거래 내역 테이블 */}
                  {isTradesLoading || isRentsLoading ? (
                    <div className="flex items-center justify-center py-12">
                      <Loader2 className="h-6 w-6 animate-spin text-blue-600" />
                    </div>
                  ) : (
                    <TransactionTable
                      trades={tradesData?.content ?? []}
                      rents={rentsData?.content ?? []}
                      transactionType={transactionType}
                    />
                  )}
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>

          {/* 가격 비교 카드 */}
          <PriceSummaryCard
            complexId={numericId}
            unitTypeId={unitTypeIdNum}
            transactionType={transactionType}
          />
        </div>
      </div>
    </div>
  );
}

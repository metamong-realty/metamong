'use client';

import { useMemo, useState } from 'react';
import Link from 'next/link';

import {
  ArrowLeft,
  Building2,
  Calendar,
  Car,
  Check,
  Copy,
  Home,
  Layers,
  Loader2,
  MapPin,
  Ruler,
} from 'lucide-react';

import { AptPriceChart, type ChartDataPoint } from '@/components/apt-price-chart';
import { PriceSummaryCard } from '@/components/price-summary-card';
import { TransactionTable } from '@/components/transaction-table';
import { Badge } from '@/components/ui/badge';
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
import type { TransactionTypeFilter } from '@/types';

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

  // 사용자가 직접 선택했으면 그 값, 아니면 첫 번째 평형을 기본값으로
  const selectedUnitTypeId =
    manualUnitTypeId || (unitTypes.length > 0 ? String(unitTypes[0].unitTypeId) : '');

  const unitTypeIdNum = selectedUnitTypeId ? parseInt(selectedUnitTypeId, 10) : undefined;
  const { data: tradesData } = useGetTrades(numericId, { unitTypeId: unitTypeIdNum });
  const { data: rentsData } = useGetRents(numericId, { unitTypeId: unitTypeIdNum });
  const { data: tradeChartData } = useGetTradeChart(numericId, { unitTypeId: unitTypeIdNum });
  const { data: rentChartData } = useGetRentChart(numericId, { unitTypeId: unitTypeIdNum });

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

  const address = complex.addressRoad ?? complex.addressJibun ?? '';

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
          {/* 단지 기본 정보 카드 */}
          <Card>
            <CardContent className="p-5">
              {/* 주소 */}
              {address && (
                <div className="mb-4 flex items-center text-sm text-gray-600">
                  <MapPin className="mr-1.5 h-4 w-4 shrink-0" />
                  <span>{address}</span>
                  <CopyButton text={address} />
                </div>
              )}

              {/* 상세 정보 그리드 */}
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                {complex.builtYear && (
                  <InfoItem icon={Calendar} label="준공년도" value={`${complex.builtYear}년`} />
                )}
                {complex.totalHousehold && (
                  <InfoItem
                    icon={Home}
                    label="총 세대수"
                    value={`${complex.totalHousehold.toLocaleString()}세대`}
                  />
                )}
                {complex.totalBuilding && (
                  <InfoItem icon={Layers} label="총 동수" value={`${complex.totalBuilding}동`} />
                )}
                {complex.totalParking && (
                  <InfoItem
                    icon={Car}
                    label="주차대수"
                    value={`${complex.totalParking.toLocaleString()}대`}
                  />
                )}
                {complex.floorAreaRatio && (
                  <InfoItem icon={Ruler} label="용적률" value={`${complex.floorAreaRatio}%`} />
                )}
                {complex.buildingCoverageRatio && (
                  <InfoItem
                    icon={Ruler}
                    label="건폐율"
                    value={`${complex.buildingCoverageRatio}%`}
                  />
                )}
              </div>

              {complex.heatingType && (
                <div className="mt-4">
                  <Badge variant="secondary">{complex.heatingType}</Badge>
                </div>
              )}
            </CardContent>
          </Card>

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
                  <TransactionTable
                    trades={tradesData?.content ?? []}
                    rents={rentsData?.content ?? []}
                    transactionType={transactionType}
                  />
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

function InfoItem({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center gap-2 rounded-lg bg-gray-50 px-3 py-2">
      <Icon className="h-4 w-4 text-gray-400" />
      <div>
        <p className="text-xs text-gray-500">{label}</p>
        <p className="text-sm font-medium text-gray-900">{value}</p>
      </div>
    </div>
  );
}

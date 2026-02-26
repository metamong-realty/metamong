'use client';

import { useMemo } from 'react';

import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Title,
  Tooltip,
  type TooltipItem,
} from 'chart.js';
import { Chart } from 'react-chartjs-2';

import { formatPrice } from '@/lib/format';
import type { TransactionTypeFilter } from '@/types';

// Chart.js에 사용할 요소들을 등록
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
);

export interface ChartDataPoint {
  month: string;
  saleAvgPrice: number | null;
  leaseAvgPrice: number | null;
  saleCount: number;
  leaseCount: number;
}

interface AptPriceChartProps {
  chartData: ChartDataPoint[];
  transactionType: TransactionTypeFilter;
}

export function AptPriceChart({ chartData, transactionType }: AptPriceChartProps) {
  const showSale = transactionType === '전체' || transactionType === '매매';
  const showLease = transactionType === '전체' || transactionType === '전세';

  const { labels, datasets } = useMemo(() => {
    const labels = chartData.map((d) => d.month);
    const datasets = [];

    // 막대 차트 (거래량) — order 2로 뒤에 그려짐
    if (showSale) {
      datasets.push({
        type: 'bar' as const,
        label: '매매 거래량',
        data: chartData.map((d) => d.saleCount),
        backgroundColor: 'rgba(96, 165, 250, 0.5)',
        borderRadius: 4,
        yAxisID: 'y1',
        order: 2,
      });
    }
    if (showLease) {
      datasets.push({
        type: 'bar' as const,
        label: '전세 거래량',
        data: chartData.map((d) => d.leaseCount),
        backgroundColor: 'rgba(251, 146, 60, 0.5)',
        borderRadius: 4,
        yAxisID: 'y1',
        order: 2,
      });
    }

    // 라인 차트 (가격) — order 1로 앞에 그려짐
    if (showSale) {
      datasets.push({
        type: 'line' as const,
        label: '매매 가격',
        data: chartData.map((d) => d.saleAvgPrice),
        borderColor: '#2563eb',
        backgroundColor: '#2563eb',
        borderWidth: 3,
        pointRadius: 4,
        pointHoverRadius: 6,
        tension: 0.1,
        yAxisID: 'y',
        order: 1,
        spanGaps: true,
      });
    }
    if (showLease) {
      datasets.push({
        type: 'line' as const,
        label: '전세 가격',
        data: chartData.map((d) => d.leaseAvgPrice),
        borderColor: '#ea580c',
        backgroundColor: '#ea580c',
        borderWidth: 3,
        pointRadius: 4,
        pointHoverRadius: 6,
        tension: 0.1,
        yAxisID: 'y',
        order: 1,
        spanGaps: true,
      });
    }

    return { labels, datasets };
  }, [chartData, showSale, showLease]);

  const options = useMemo(
    () => ({
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index' as const, intersect: false },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(255,255,255,0.95)',
          titleColor: '#111827',
          bodyColor: '#374151',
          borderColor: '#e5e7eb',
          borderWidth: 1,
          padding: 12,
          callbacks: {
            label: (context: TooltipItem<'line' | 'bar'>) => {
              const label = context.dataset.label ?? '';
              const value = context.parsed.y;
              if (value === null) return '';
              return label.includes('가격')
                ? `${label}: ${formatPrice(value)}`
                : `${label}: ${value}건`;
            },
          },
        },
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { maxRotation: 45, minRotation: 45, font: { size: 11 } },
        },
        y: {
          type: 'linear' as const,
          display: true,
          position: 'left' as const,
          title: { display: true, text: '가격', font: { size: 12 } },
          ticks: {
            callback: (value: number | string) =>
              `${Math.round((typeof value === 'string' ? parseFloat(value) : value) / 10000)}억`,
            font: { size: 11 },
          },
          grid: { color: 'rgba(229,231,235,0.5)' },
        },
        y1: {
          type: 'linear' as const,
          display: true,
          position: 'right' as const,
          title: { display: true, text: '거래량', font: { size: 12 } },
          min: 0,
          suggestedMax: 10,
          ticks: { stepSize: 2, font: { size: 11 } },
          grid: { drawOnChartArea: false },
        },
      },
    }),
    [],
  );

  if (chartData.length === 0) {
    return (
      <div className="flex h-[300px] items-center justify-center text-gray-500">거래 내역 없음</div>
    );
  }

  return (
    <div className="space-y-4">
      {/* 커스텀 범례 */}
      <div className="flex flex-wrap items-center gap-4 text-sm">
        {showSale && (
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="h-0.5 w-6 bg-blue-600" />
              <span>매매 가격</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded-sm bg-blue-400 opacity-60" />
              <span>매매 거래량</span>
            </div>
          </div>
        )}
        {showLease && (
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="h-0.5 w-6 bg-orange-600" />
              <span>전세 가격</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded-sm bg-orange-400 opacity-60" />
              <span>전세 거래량</span>
            </div>
          </div>
        )}
      </div>

      {/* 차트 */}
      <div className="h-[350px] w-full sm:h-[400px]">
        <Chart type="bar" data={{ labels, datasets }} options={options} />
      </div>
    </div>
  );
}

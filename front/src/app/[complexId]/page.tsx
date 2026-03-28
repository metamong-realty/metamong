import type { Metadata } from 'next';
import { dehydrate, HydrationBoundary, QueryClient } from '@tanstack/react-query';

import { ComplexDetail } from '@/components/complex-detail';
import type { ComplexDetail as ComplexDetailType } from '@/types';

interface ComplexDetailPageProps {
  params: Promise<{ complexId: string }>;
}

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://metamong-server-production.up.railway.app';

async function serverFetch<T>(path: string): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    next: { revalidate: 3600 },
  });
  if (!res.ok) throw new Error(`API Error: ${res.status}`);
  const json = await res.json();
  return json.data;
}

export async function generateMetadata({ params }: ComplexDetailPageProps): Promise<Metadata> {
  const { complexId } = await params;
  try {
    const complex = await serverFetch<ComplexDetailType>(`/v1/apartments/complexes/${complexId}`);
    const address = complex.addressRoad ?? complex.addressJibun ?? '';
    return {
      title: `${complex.name} 실거래가 | 메타몽`,
      description: `${address} ${complex.name}의 실거래가, 시세, 매매/전세 정보`,
      openGraph: {
        title: `${complex.name} 실거래가`,
        description: `${address} ${complex.name}의 실거래가, 시세, 매매/전세 정보`,
        images: [
          {
            url: `/api/og?complexId=${complexId}`,
            width: 1200,
            height: 630,
            alt: `${complex.name} 실거래가`,
          },
        ],
      },
    };
  } catch {
    return {
      title: '단지 상세 | 메타몽',
    };
  }
}

export default async function ComplexDetailPage({ params }: ComplexDetailPageProps) {
  const { complexId: complexIdStr } = await params;
  const id = Number(complexIdStr);

  const queryClient = new QueryClient();

  await Promise.allSettled([
    queryClient.prefetchQuery({
      queryKey: ['complexes', id, 'detail'],
      queryFn: () => serverFetch<ComplexDetailType>(`/v1/apartments/complexes/${id}`),
    }),
    queryClient.prefetchQuery({
      queryKey: ['complexes', id, 'unitTypes'],
      queryFn: () => serverFetch(`/v1/apartments/complexes/${id}/unit-types`),
    }),
    queryClient.prefetchQuery({
      queryKey: [
        'complexes',
        id,
        'trades',
        { unitTypeId: undefined, period: 'RECENT_3YEARS', page: 0, size: 100 },
      ],
      queryFn: () =>
        serverFetch(`/v1/apartments/complexes/${id}/trades?period=RECENT_3YEARS&page=0&size=100`),
    }),
    queryClient.prefetchQuery({
      queryKey: [
        'complexes',
        id,
        'rents',
        { unitTypeId: undefined, period: 'RECENT_3YEARS', rentType: undefined, page: 0, size: 100 },
      ],
      queryFn: () =>
        serverFetch(`/v1/apartments/complexes/${id}/rents?period=RECENT_3YEARS&page=0&size=100`),
    }),
    queryClient.prefetchQuery({
      queryKey: [
        'complexes',
        id,
        'trades',
        'chart',
        { unitTypeId: undefined, period: 'RECENT_3YEARS' },
      ],
      queryFn: () =>
        serverFetch(`/v1/apartments/complexes/${id}/trades/chart?period=RECENT_3YEARS`),
    }),
    queryClient.prefetchQuery({
      queryKey: [
        'complexes',
        id,
        'rents',
        'chart',
        { unitTypeId: undefined, period: 'RECENT_3YEARS' },
      ],
      queryFn: () => serverFetch(`/v1/apartments/complexes/${id}/rents/chart?period=RECENT_3YEARS`),
    }),
  ]);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <ComplexDetail complexId={complexIdStr} />
    </HydrationBoundary>
  );
}

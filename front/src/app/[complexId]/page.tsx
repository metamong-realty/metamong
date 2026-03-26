import type { Metadata } from 'next';

import { ComplexDetail } from '@/components/complex-detail';
import type { ComplexDetail as ComplexDetailType } from '@/types';

interface ComplexDetailPageProps {
  params: Promise<{ complexId: string }>;
}

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://metamong-server-production.up.railway.app';

async function fetchComplexDetail(complexId: string): Promise<ComplexDetailType | null> {
  try {
    const res = await fetch(`${API_BASE_URL}/v1/apartments/complexes/${complexId}`, {
      next: { revalidate: 3600 },
    });
    if (!res.ok) return null;
    const json = await res.json();
    return json.data;
  } catch {
    return null;
  }
}

export async function generateMetadata({ params }: ComplexDetailPageProps): Promise<Metadata> {
  const { complexId } = await params;
  const complex = await fetchComplexDetail(complexId);

  if (!complex) {
    return {
      title: '단지 상세 | 메타몽',
      description: '아파트 매매·전월세 실거래가를 확인하세요.',
    };
  }

  const address = complex.addressRoad ?? complex.addressJibun ?? '';
  const title = `${complex.name} 실거래가 | 메타몽`;
  const description = `${complex.name} 매매·전월세 실거래가를 확인하세요.${address ? ` ${address}` : ''}`;

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      type: 'website',
    },
  };
}

export default async function ComplexDetailPage({ params }: ComplexDetailPageProps) {
  const { complexId } = await params;

  return <ComplexDetail complexId={complexId} />;
}

import { ComplexDetail } from '@/components/complex-detail';

interface ComplexDetailPageProps {
  params: Promise<{ complexId: string }>;
}

export default async function ComplexDetailPage({ params }: ComplexDetailPageProps) {
  const { complexId } = await params;

  return <ComplexDetail complexId={complexId} />;
}

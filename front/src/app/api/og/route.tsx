import { ImageResponse } from 'next/og';
import type { NextRequest } from 'next/server';

export const runtime = 'edge';

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://metamong-server-production.up.railway.app';

function formatPrice(manwon: number): string {
  const eok = Math.floor(manwon / 10000);
  const man = manwon % 10000;
  if (eok > 0 && man > 0) return `${eok}억 ${man.toLocaleString()}만`;
  if (eok > 0) return `${eok}억`;
  return `${manwon.toLocaleString()}만`;
}

interface ComplexData {
  name: string;
  addressRoad: string | null;
  addressJibun: string | null;
}

interface PriceSummaryData {
  trade: {
    recentMonthAvgPrice: number;
    priceChangeRate: number | null;
  } | null;
  rent: {
    recentMonthAvgDeposit: number;
    depositChangeRate: number | null;
  } | null;
}

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);
  const complexId = searchParams.get('complexId');

  if (!complexId) {
    return new Response('Missing complexId', { status: 400 });
  }

  const [fontResult, complexRes, priceRes] = await Promise.allSettled([
    fetch(
      'https://fonts.gstatic.com/s/notosanskr/v36/PbyxFmXiEBPT4ITbgNA5Cgm20xz64px_1hVWr0wuPNGmlQNMEfD4.0.woff2',
    ).then((r) => r.arrayBuffer()),
    fetch(`${API_BASE_URL}/v1/apartments/complexes/${complexId}`, {
      next: { revalidate: 3600 },
    }),
    fetch(
      `${API_BASE_URL}/v1/apartments/complexes/${complexId}/price-summary?lookbackMonths=3`,
      { next: { revalidate: 3600 } },
    ),
  ]);

  const fontData = fontResult.status === 'fulfilled' ? fontResult.value : new ArrayBuffer(0);

  let complex: ComplexData | null = null;
  let priceSummary: PriceSummaryData | null = null;

  if (complexRes.status === 'fulfilled' && complexRes.value.ok) {
    const json = await complexRes.value.json();
    complex = json.data;
  }

  if (priceRes.status === 'fulfilled' && priceRes.value.ok) {
    const json = await priceRes.value.json();
    priceSummary = json.data;
  }

  const name = complex?.name ?? '단지 정보';
  const address = complex?.addressRoad ?? complex?.addressJibun ?? '';

  const tradeAvg = priceSummary?.trade?.recentMonthAvgPrice ?? null;
  const tradeRate = priceSummary?.trade?.priceChangeRate ?? null;
  const rentAvg = priceSummary?.rent?.recentMonthAvgDeposit ?? null;
  const rentRate = priceSummary?.rent?.depositChangeRate ?? null;
  const hasPrice = tradeAvg !== null || rentAvg !== null;

  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'space-between',
          backgroundColor: 'white',
          padding: '60px',
          fontFamily: 'NotoSansKR',
        }}
      >
        {/* 상단: 브랜드 */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <div style={{ fontSize: 24, color: '#2563EB', display: 'flex' }}>🏠 메타몽</div>

          {/* 단지명 */}
          <div
            style={{
              fontSize: 64,
              fontWeight: 700,
              color: '#000000',
              marginTop: 40,
              display: 'flex',
            }}
          >
            {name}
          </div>

          {/* 주소 */}
          {address && (
            <div
              style={{
                fontSize: 28,
                color: '#6B7280',
                marginTop: 12,
                display: 'flex',
              }}
            >
              {address}
            </div>
          )}

          {/* 가격 영역 */}
          <div style={{ marginTop: 40, display: 'flex', flexDirection: 'column', gap: 16 }}>
            {hasPrice ? (
              <>
                {tradeAvg !== null && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                    <div style={{ fontSize: 32, color: '#111827', display: 'flex' }}>
                      매매 평균 {formatPrice(tradeAvg)}
                    </div>
                    {tradeRate !== null && (
                      <div
                        style={{
                          fontSize: 28,
                          color: tradeRate >= 0 ? '#16A34A' : '#DC2626',
                          display: 'flex',
                        }}
                      >
                        {tradeRate >= 0 ? '▲' : '▼'} {Math.abs(tradeRate).toFixed(1)}%
                      </div>
                    )}
                  </div>
                )}
                {rentAvg !== null && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                    <div style={{ fontSize: 32, color: '#111827', display: 'flex' }}>
                      전세 평균 {formatPrice(rentAvg)}
                    </div>
                    {rentRate !== null && (
                      <div
                        style={{
                          fontSize: 28,
                          color: rentRate >= 0 ? '#16A34A' : '#DC2626',
                          display: 'flex',
                        }}
                      >
                        {rentRate >= 0 ? '▲' : '▼'} {Math.abs(rentRate).toFixed(1)}%
                      </div>
                    )}
                  </div>
                )}
              </>
            ) : (
              <div style={{ fontSize: 32, color: '#6B7280', display: 'flex' }}>
                실거래가 조회하기
              </div>
            )}
          </div>
        </div>

        {/* 우하단: 도메인 */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'flex-end',
            fontSize: 20,
            color: '#6B7280',
          }}
        >
          metamong.kr
        </div>
      </div>
    ),
    {
      width: 1200,
      height: 630,
      fonts: [
        {
          name: 'NotoSansKR',
          data: fontData,
          style: 'normal',
          weight: 400,
        },
      ],
    },
  );
}

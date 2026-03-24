import { type NextRequest, NextResponse } from 'next/server';

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  'https://metamong-server-production.up.railway.app';

async function handler(req: NextRequest, { params }: { params: Promise<{ path: string[] }> }) {
  const { path } = await params;
  const targetUrl = `${API_BASE_URL}/${path.join('/')}${req.nextUrl.search}`;

  // 요청 헤더 복사 (host 제외)
  const headers = new Headers(req.headers);
  headers.delete('host');

  // cookie 헤더 전달 (refreshToken 포함)
  const upstreamRes = await fetch(targetUrl, {
    method: req.method,
    headers,
    body: req.method !== 'GET' && req.method !== 'HEAD' ? await req.blob() : undefined,
    redirect: 'manual',
  });

  // 응답 헤더 복사
  const resHeaders = new Headers(upstreamRes.headers);

  // Set-Cookie 헤더 명시적으로 전달 (Vercel rewrites는 자동 전달 안 함)
  const setCookies = upstreamRes.headers.getSetCookie?.() ?? [];
  if (setCookies.length > 0) {
    // 기존 Set-Cookie 제거 후 개별 추가
    resHeaders.delete('set-cookie');
  }

  const res = new NextResponse(upstreamRes.body, {
    status: upstreamRes.status,
    headers: resHeaders,
  });

  // Set-Cookie 개별 추가
  for (const cookie of setCookies) {
    res.headers.append('set-cookie', cookie);
  }

  return res;
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;

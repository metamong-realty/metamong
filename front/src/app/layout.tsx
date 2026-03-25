import type { Metadata, Viewport } from 'next';
import localFont from 'next/font/local';

import { Providers } from './providers';
import './globals.css';

const geistSans = localFont({
  src: [
    { path: '../../public/fonts/geist-latin.woff2', weight: '100 900' },
    { path: '../../public/fonts/geist-latin-ext.woff2', weight: '100 900' },
  ],
  variable: '--font-geist-sans',
});

const geistMono = localFont({
  src: [
    { path: '../../public/fonts/geist-mono-latin.woff2', weight: '100 900' },
    { path: '../../public/fonts/geist-mono-latin-ext.woff2', weight: '100 900' },
  ],
  variable: '--font-geist-mono',
});

export const metadata: Metadata = {
  title: '아파트 실거래가 조회',
  description: '아파트 매매/전세 실거래가를 조회하세요',
};

// 모바일 viewport 설정 — 페이지 전체 줌 방지, 차트 핀치줌은 JS로 별도 처리
export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}

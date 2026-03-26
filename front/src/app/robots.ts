import type { MetadataRoute } from 'next';

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL ?? 'https://metamong.kr';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: [
      {
        userAgent: 'Googlebot',
        allow: '/',
        disallow: ['/login', '/oauth'],
      },
      {
        userAgent: '*',
        allow: '/',
        disallow: ['/login', '/oauth'],
      },
    ],
    sitemap: `${SITE_URL}/sitemap.xml`,
  };
}

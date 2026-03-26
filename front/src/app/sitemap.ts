import type { MetadataRoute } from 'next';

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL ?? 'https://metamong.kr';
const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://metamong-server-production.up.railway.app';

async function fetchSitemapIds(): Promise<number[]> {
  try {
    const res = await fetch(`${API_BASE_URL}/v1/apartments/complexes/sitemap-ids`, {
      next: { revalidate: 86400 },
    });
    if (!res.ok) return [];
    const json = await res.json();
    return json.data ?? [];
  } catch {
    return [];
  }
}

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const complexIds = await fetchSitemapIds();

  const complexEntries: MetadataRoute.Sitemap = complexIds.map((id) => ({
    url: `${SITE_URL}/${id}`,
    lastModified: new Date(),
    changeFrequency: 'weekly',
    priority: 0.8,
  }));

  return [
    {
      url: SITE_URL,
      lastModified: new Date(),
      changeFrequency: 'daily',
      priority: 1.0,
    },
    ...complexEntries,
  ];
}

import { getMockData } from '@/lib/mock-data';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL;
const USE_MOCK = process.env.NEXT_PUBLIC_USE_MOCK === 'true';

// BE에 아직 없는 API는 mock으로 fallback
const MOCK_ONLY_PREFIXES = ['/v1/apartments/regions/'];

function shouldUseMock(path: string): boolean {
  if (USE_MOCK) return true;
  return MOCK_ONLY_PREFIXES.some((prefix) => path.startsWith(prefix));
}

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  if (shouldUseMock(path)) {
    await new Promise((resolve) => setTimeout(resolve, 300));
    return getMockData(path) as T;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!res.ok) {
    throw new Error(`API Error: ${res.status}`);
  }

  const json = await res.json();
  return json.data;
}

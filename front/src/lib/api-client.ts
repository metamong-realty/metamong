// Vercel rewrites를 통해 /api/* → BE 프록시 (same-origin cookie 해결)
const API_BASE = '/api';

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: HeadersInit = { ...options?.headers };
  const method = options?.method?.toUpperCase();
  if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
    (headers as Record<string, string>)['Content-Type'] = 'application/json';
  }

  const res = await fetch(`${API_BASE}${path}`, {
    credentials: 'include', // same-origin이라 cookie 정상 전송
    headers,
    ...options,
  });

  if (!res.ok) {
    throw new Error(`API Error: ${res.status}`);
  }

  const json = await res.json();
  return json.data;
}

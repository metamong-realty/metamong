const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL;

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: HeadersInit = { ...options?.headers };
  const method = options?.method?.toUpperCase();
  if (method === 'POST' || method === 'PUT' || method === 'PATCH') {
    (headers as Record<string, string>)['Content-Type'] = 'application/json';
  }

  const res = await fetch(`${API_BASE}${path}`, {
    headers,
    ...options,
  });

  if (!res.ok) {
    throw new Error(`API Error: ${res.status}`);
  }

  const json = await res.json();
  return json.data;
}

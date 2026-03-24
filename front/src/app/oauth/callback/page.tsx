'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

import { Loader2 } from 'lucide-react';

import { useAuth } from '@/lib/auth-context';

export default function OAuthCallbackPage() {
  const router = useRouter();
  const { setAccessToken } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');
    const redirectTo = sessionStorage.getItem('oauth_redirect') ?? '/';
    sessionStorage.removeItem('oauth_redirect');

    if (!code) {
      router.replace(redirectTo);
      return;
    }

    // code → token exchange (API Route proxy 통해 → Set-Cookie가 FE 도메인으로 set)
    fetch('/api/v1/auth/exchange', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ code }),
    })
      .then((res) => res.json())
      .then((json) => {
        if (json.data?.accessToken) {
          setAccessToken(json.data.accessToken);
        }
      })
      .catch(() => {})
      .finally(() => {
        router.replace(redirectTo);
      });
  }, [setAccessToken, router]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
    </div>
  );
}

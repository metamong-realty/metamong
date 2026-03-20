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
    const accessToken = params.get('accessToken');
    const redirectTo = sessionStorage.getItem('oauth_redirect') ?? '/';
    sessionStorage.removeItem('oauth_redirect');

    if (accessToken) {
      setAccessToken(accessToken);
    }

    router.replace(redirectTo);
  }, [setAccessToken, router]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
    </div>
  );
}

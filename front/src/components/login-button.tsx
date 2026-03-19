'use client';

import { useRouter } from 'next/navigation';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL;

const PROVIDERS = [
  {
    id: 'kakao',
    label: '카카오로 로그인',
    bg: 'bg-[#FEE500] hover:bg-[#F0D800]',
    text: 'text-[#3C1E1E]',
    icon: '💬',
  },
  {
    id: 'naver',
    label: '네이버로 로그인',
    bg: 'bg-[#03C75A] hover:bg-[#02B350]',
    text: 'text-white',
    icon: 'N',
  },
  {
    id: 'google',
    label: 'Google로 로그인',
    bg: 'bg-white hover:bg-gray-50 border border-gray-300',
    text: 'text-gray-700',
    icon: 'G',
  },
] as const;

export function LoginButton() {
  const router = useRouter();

  const handleLogin = (provider: string) => {
    // 현재 페이지 저장 (로그인 후 복원)
    sessionStorage.setItem('oauth_redirect', window.location.pathname + window.location.search);
    // BE OAuth2 endpoint로 이동
    window.location.href = `${API_BASE}/oauth2/authorization/${provider}`;
  };

  return (
    <div className="flex flex-col gap-3">
      {PROVIDERS.map((p) => (
        <button
          key={p.id}
          onClick={() => handleLogin(p.id)}
          className={`flex items-center justify-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-colors ${p.bg} ${p.text}`}
        >
          <span className="w-5 text-center font-bold">{p.icon}</span>
          {p.label}
        </button>
      ))}
    </div>
  );
}

'use client';

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';

import { apiFetch } from '@/lib/api-client';
import type { User } from '@/types';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  isLoading: boolean;
}

interface AuthContextValue extends AuthState {
  setAccessToken: (token: string) => void;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<string | null>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    accessToken: null,
    isLoading: true,
  });

  const refreshTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const scheduleRefresh = useCallback((expiresIn: number) => {
    if (refreshTimerRef.current) clearTimeout(refreshTimerRef.current);
    // 만료 30초 전에 silent refresh
    const delay = Math.max(expiresIn - 30_000, 0);
    refreshTimerRef.current = setTimeout(() => {
      refreshAccessToken();
    }, delay);
  }, []); // eslint-disable-line

  const refreshAccessToken = useCallback(async (): Promise<string | null> => {
    try {
      const data = await apiFetch<{ accessToken: string; expiresIn: number }>(
        '/v1/auth/refresh',
        { method: 'POST', credentials: 'include' },
      );
      setState((prev) => ({ ...prev, accessToken: data.accessToken }));
      scheduleRefresh(data.expiresIn);
      return data.accessToken;
    } catch {
      setState({ user: null, accessToken: null, isLoading: false });
      return null;
    }
  }, [scheduleRefresh]);

  const setAccessToken = useCallback(
    (token: string) => {
      setState((prev) => ({ ...prev, accessToken: token }));
      // 앱 기본 access token expiry: 1시간
      scheduleRefresh(3_600_000);
      // 유저 정보 fetch
      apiFetch<User>('/v1/auth/me', {
        headers: { Authorization: `Bearer ${token}` },
      })
        .then((user) => setState((prev) => ({ ...prev, user })))
        .catch(() => {});
    },
    [scheduleRefresh],
  );

  const logout = useCallback(async () => {
    if (refreshTimerRef.current) clearTimeout(refreshTimerRef.current);
    try {
      await apiFetch('/v1/auth/logout', {
        method: 'POST',
        credentials: 'include',
        headers: state.accessToken
          ? { Authorization: `Bearer ${state.accessToken}` }
          : {},
      });
    } catch {
      // 실패해도 로컬 상태는 초기화
    }
    setState({ user: null, accessToken: null, isLoading: false });
  }, [state.accessToken]);

  // 앱 초기화: refresh token cookie로 silent refresh 시도
  useEffect(() => {
    // 개발 환경 mock user (스크린샷/테스트용)
    if (process.env.NEXT_PUBLIC_DEV_MOCK_USER === 'true') {
      setState({
        user: { id: 1, email: 'dev@metamong.com', name: '개발자', profileImageUrl: null },
        accessToken: 'mock-token',
        isLoading: false,
      });
      return;
    }
    refreshAccessToken().finally(() =>
      setState((prev) => ({ ...prev, isLoading: false })),
    );
    return () => {
      if (refreshTimerRef.current) clearTimeout(refreshTimerRef.current);
    };
  }, []); // eslint-disable-line

  return (
    <AuthContext.Provider value={{ ...state, setAccessToken, logout, refreshAccessToken }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

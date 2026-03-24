import { LoginButton } from '@/components/login-button';

export default function LoginPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="mb-2 text-center text-2xl font-bold text-gray-900">로그인</h1>
        <p className="mb-8 text-center text-sm text-gray-500">
          소셜 계정으로 간편하게 시작하세요
        </p>
        <LoginButton />
      </div>
    </div>
  );
}

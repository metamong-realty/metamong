import { Suspense } from 'react';

import { Loader2 } from 'lucide-react';

import { AptListPage } from '@/components/apt-list-page';

export default function HomePage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center">
          <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
        </div>
      }
    >
      <AptListPage />
    </Suspense>
  );
}

import { Building2, ChevronRight, MapPin } from 'lucide-react';

import { Card, CardContent } from '@/components/ui/card';
import type { ComplexListItem } from '@/types';

interface ComplexCardProps {
  complex: ComplexListItem;
}

export function ComplexCard({ complex }: ComplexCardProps) {
  return (
    <Card className="h-full transition-all hover:-translate-y-1 hover:shadow-lg">
      <CardContent className="p-5">
        <div className="mb-3 flex items-start justify-between">
          <div className="flex items-center gap-2">
            <Building2 className="h-5 w-5 text-blue-600" />
            <h3 className="font-semibold text-gray-900">{complex.name}</h3>
          </div>
          <ChevronRight className="h-5 w-5 text-gray-400" />
        </div>

        <div className="space-y-2 text-sm text-gray-600">
          {complex.builtYear && <p>{complex.builtYear}년 준공</p>}

          {complex.totalHousehold && <p>총 {complex.totalHousehold.toLocaleString()}세대</p>}

          {complex.addressShort && (
            <div className="flex items-center gap-1">
              <MapPin className="h-3.5 w-3.5" />
              <p className="truncate">{complex.addressShort}</p>
            </div>
          )}

          <div className="mt-3 flex items-center gap-3 border-t pt-3 text-xs text-gray-500">
            <span>거래 {complex.totalTradeCount.toLocaleString()}건</span>
            <span className="text-blue-600">
              최근 3년 {complex.recent3YearsTradeCount.toLocaleString()}건
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

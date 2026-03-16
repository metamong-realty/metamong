import { Building2, Calendar, Car, Home, Layers, Ruler } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

interface ApartmentSpecsProps {
  builtYear?: number | null;
  totalHousehold?: number | null;
  totalBuilding?: number | null;
  totalParking?: number | null;
  floorAreaRatio?: number | null;
  buildingCoverageRatio?: number | null;
  heatingType?: string | null;
}

export function ApartmentSpecs({
  builtYear,
  totalHousehold,
  totalBuilding,
  totalParking,
  floorAreaRatio,
  buildingCoverageRatio,
  heatingType,
}: ApartmentSpecsProps) {
  return (
    <Card>
      <CardContent className="p-5">
        <div className="mb-4 flex items-center gap-2">
          <Building2 className="h-5 w-5 text-blue-600" />
          <h2 className="text-lg font-semibold text-gray-900">단지 정보</h2>
        </div>

        {/* 상세 정보 그리드 */}
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {builtYear && <InfoItem icon={Calendar} label="준공년도" value={`${builtYear}년`} />}
          {totalHousehold && (
            <InfoItem
              icon={Home}
              label="총 세대수"
              value={`${totalHousehold.toLocaleString()}세대`}
            />
          )}
          {totalBuilding && <InfoItem icon={Layers} label="총 동수" value={`${totalBuilding}동`} />}
          {totalParking && (
            <InfoItem icon={Car} label="주차대수" value={`${totalParking.toLocaleString()}대`} />
          )}
          {floorAreaRatio && <InfoItem icon={Ruler} label="용적률" value={`${floorAreaRatio}%`} />}
          {buildingCoverageRatio && (
            <InfoItem icon={Ruler} label="건폐율" value={`${buildingCoverageRatio}%`} />
          )}
        </div>

        {heatingType && (
          <div className="mt-4">
            <Badge variant="secondary">{heatingType}</Badge>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function InfoItem({
  icon: Icon,
  label,
  value,
}: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string;
}) {
  return (
    <div className="flex items-center gap-2 rounded-lg bg-gray-50 px-3 py-2">
      <Icon className="h-4 w-4 text-gray-400" />
      <div>
        <p className="text-xs text-gray-500">{label}</p>
        <p className="text-sm font-medium text-gray-900">{value}</p>
      </div>
    </div>
  );
}

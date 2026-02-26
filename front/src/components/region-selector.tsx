'use client';

import { useMemo, useState } from 'react';

import { MapPin } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { useGetEupmyeondongList, useGetSidoList, useGetSigunguList } from '@/hooks/use-regions';
import { cn } from '@/lib/utils';
import type { Region } from '@/types';

interface RegionSelectorProps {
  sidoCode: string;
  sigunguCode: string;
  eupmyeondongCode: string;
  onSidoChange: (code: string) => void;
  onSigunguChange: (code: string) => void;
  onEupmyeondongChange: (code: string) => void;
}

export function RegionSelector({
  sidoCode,
  sigunguCode,
  eupmyeondongCode,
  onSidoChange,
  onSigunguChange,
  onEupmyeondongChange,
}: RegionSelectorProps) {
  const { data: sidoList = [], isLoading: isSidoLoading } = useGetSidoList();
  const { data: sigunguList = [], isLoading: isSigunguLoading } = useGetSigunguList(sidoCode);
  const { data: eupmyeondongList = [], isLoading: isEupmyeondongLoading } =
    useGetEupmyeondongList(sigunguCode);

  // --- 모바일 Sheet 상태 ---
  const [isSheetOpen, setIsSheetOpen] = useState(false);
  const [mobileStep, setMobileStep] = useState<'sido' | 'sigungu' | 'eupmyeondong'>('sido');

  const selectedSido = sidoList.find((s) => s.code === sidoCode);
  const selectedSigungu = sigunguList.find((s) => s.code === sigunguCode);
  const selectedEupmyeondong = eupmyeondongList.find((e) => e.code === eupmyeondongCode);

  const currentSelectionText = useMemo(() => {
    if (selectedSido && selectedSigungu && selectedEupmyeondong) {
      return `${selectedSido.name} ${selectedSigungu.name} ${selectedEupmyeondong.name}`;
    }
    if (selectedSido && selectedSigungu) {
      return `${selectedSido.name} ${selectedSigungu.name}`;
    }
    if (selectedSido) {
      return selectedSido.name;
    }
    return '지역을 선택해주세요';
  }, [selectedSido, selectedSigungu, selectedEupmyeondong]);

  // --- 모바일 핸들러 ---
  const openSheet = () => {
    if (sidoCode && !sigunguCode) {
      setMobileStep('sigungu');
    } else if (sigunguCode && !eupmyeondongCode) {
      setMobileStep('eupmyeondong');
    } else {
      setMobileStep('sido');
    }
    setIsSheetOpen(true);
  };

  const handleMobileSelect = (step: 'sido' | 'sigungu' | 'eupmyeondong', code: string) => {
    if (step === 'sido') {
      onSidoChange(code);
      setMobileStep('sigungu');
    } else if (step === 'sigungu') {
      onSigunguChange(code);
      setMobileStep('eupmyeondong');
    } else {
      onEupmyeondongChange(code);
      setIsSheetOpen(false);
    }
  };

  const mobileStepConfig = {
    sido: { title: '시/도 선택', list: sidoList, selectedCode: sidoCode },
    sigungu: { title: '시/군/구 선택', list: sigunguList, selectedCode: sigunguCode },
    eupmyeondong: {
      title: '읍/면/동 선택',
      list: eupmyeondongList,
      selectedCode: eupmyeondongCode,
    },
  };
  const currentStep = mobileStepConfig[mobileStep];

  return (
    <>
      {/* 데스크톱: Select 3개 */}
      <div className="hidden md:grid md:grid-cols-3 md:gap-3">
        <Select value={sidoCode} onValueChange={onSidoChange}>
          <SelectTrigger className="w-full bg-white transition-colors hover:bg-gray-50">
            <SelectValue placeholder={isSidoLoading ? '로딩중...' : '시/도 선택'} />
          </SelectTrigger>
          <SelectContent>
            {sidoList.map((sido) => (
              <SelectItem key={sido.code} value={sido.code}>
                {sido.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select value={sigunguCode} onValueChange={onSigunguChange} disabled={!sidoCode}>
          <SelectTrigger className="w-full bg-white transition-colors hover:bg-gray-50">
            <SelectValue
              placeholder={
                isSigunguLoading ? '로딩중...' : !sidoCode ? '시/도를 먼저 선택' : '시/군/구 선택'
              }
            />
          </SelectTrigger>
          <SelectContent>
            {sigunguList.map((sigungu) => (
              <SelectItem key={sigungu.code} value={sigungu.code}>
                {sigungu.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select
          value={eupmyeondongCode}
          onValueChange={onEupmyeondongChange}
          disabled={!sigunguCode}
        >
          <SelectTrigger className="w-full bg-white transition-colors hover:bg-gray-50">
            <SelectValue
              placeholder={
                isEupmyeondongLoading
                  ? '로딩중...'
                  : !sigunguCode
                    ? '시/군/구를 먼저 선택'
                    : '읍/면/동 선택 (전체)'
              }
            />
          </SelectTrigger>
          <SelectContent>
            {eupmyeondongList.map((dong) => (
              <SelectItem key={dong.code} value={dong.code}>
                {dong.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* 모바일: 버튼 + Sheet */}
      <div className="md:hidden">
        <Button variant="outline" className="w-full justify-start" onClick={openSheet}>
          <MapPin className="mr-2 h-4 w-4" />
          {currentSelectionText}
        </Button>

        <Sheet open={isSheetOpen} onOpenChange={setIsSheetOpen}>
          <SheetContent side="bottom" className="h-[60vh]">
            <SheetHeader>
              <SheetTitle>{currentStep.title}</SheetTitle>
            </SheetHeader>
            <div className="mt-4 space-y-1 overflow-y-auto">
              {currentStep.list.map((item) => (
                <button
                  key={item.code}
                  className={cn(
                    'w-full rounded-lg px-4 py-3 text-left transition-colors hover:bg-gray-100',
                    item.code === currentStep.selectedCode &&
                      'bg-blue-50 font-medium text-blue-600',
                  )}
                  onClick={() => handleMobileSelect(mobileStep, item.code)}
                >
                  {item.name}
                </button>
              ))}
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </>
  );
}

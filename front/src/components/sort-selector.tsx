'use client';

export type SortOrder = 'DEFAULT' | 'TRADE_COUNT' | 'BUILT_YEAR' | 'POPULAR';

interface SortSelectorProps {
  value: SortOrder;
  onChange: (value: SortOrder) => void;
  disabled?: boolean;
}

export function SortSelector({ value, onChange, disabled = false }: SortSelectorProps) {
  return (
    <div className="flex items-center gap-2">
      <label htmlFor="sort-order" className="text-sm font-medium text-gray-700">
        정렬:
      </label>
      <select
        id="sort-order"
        value={value}
        onChange={(e) => onChange(e.target.value as SortOrder)}
        disabled={disabled}
        className="rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm transition-colors hover:border-gray-400 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 disabled:cursor-not-allowed disabled:bg-gray-50 disabled:text-gray-400"
      >
        <option value="DEFAULT">기본순</option>
        <option value="TRADE_COUNT">거래량순</option>
        {/* 향후 확장 */}
        {/* <option value="BUILT_YEAR">최신건설순</option> */}
        {/* <option value="POPULAR">인기순</option> */}
      </select>
    </div>
  );
}

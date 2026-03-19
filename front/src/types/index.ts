// === 유저 ===
export interface User {
  id: number;
  email: string;
  name: string | null;
  profileImageUrl: string | null;
}

// === 지역 ===
export interface Region {
  name: string;
  code: string;
}

// === 단지 (목록용) ===
export interface ComplexListItem {
  complexId: number;
  name: string;
  builtYear: number | null;
  totalHousehold: number | null;
  eupmyeondongName: string | null;
  addressShort: string | null;
  totalTradeCount: number;
  recent3YearsTradeCount: number;
}

// === 단지 (상세용) ===
export interface ComplexDetail {
  id: number;
  name: string;
  addressRoad: string | null;
  addressJibun: string | null;
  builtYear: number | null;
  totalHousehold: number | null;
  totalBuilding: number | null;
  totalParking: number | null;
  floorAreaRatio: number | null;
  buildingCoverageRatio: number | null;
  heatingType: string | null;
  isSubscribed: boolean;
}

// === 평형 ===
export interface UnitType {
  unitTypeId: number;
  exclusivePyeong: number;
}

// === 매매 거래 ===
export interface Trade {
  tradeId: number;
  contractDate: string;
  exclusiveArea: number;
  exclusivePyeong: number | null;
  floor: number | null;
  price: number;
  isDirect: boolean;
  isCanceled: boolean;
}

// === 전월세 거래 ===
export interface Rent {
  rentId: number;
  contractDate: string;
  exclusiveArea: number;
  exclusivePyeong: number | null;
  floor: number | null;
  rentType: 'JEONSE' | 'MONTHLY';
  deposit: number;
  monthlyRent: number;
  isCanceled: boolean;
}

// === 차트 ===
export interface TradeChartData {
  priceChart: { yearMonth: string; avgPrice: number; maxPrice: number; minPrice: number }[];
  volumeChart: { yearMonth: string; tradeCount: number; rentCount: number }[];
}

export interface RentChartData {
  priceChart: { yearMonth: string; avgDeposit: number; maxDeposit: number; minDeposit: number }[];
  volumeChart: { yearMonth: string; tradeCount: number; rentCount: number }[];
}

// === 가격 요약 ===
export interface PriceSummary {
  lookbackMonths: number;
  trade: {
    recentMonthAvgPrice: number;
    lookbackMonthAvgPrice: number;
    priceChangeRate: number | null;
  } | null;
  rent: {
    recentMonthAvgDeposit: number;
    lookbackMonthAvgDeposit: number;
    depositChangeRate: number | null;
  } | null;
}

// === 페이징 (Spring Page 응답) ===
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
}

// === 필터 타입 ===
export type TransactionTypeFilter = '전체' | '매매' | '전세';
export type TimePeriodFilter = 'RECENT_3YEARS' | 'ALL';
export type LookbackMonths = 1 | 3 | 6 | 12 | 24 | 36 | 60;

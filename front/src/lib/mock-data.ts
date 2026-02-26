import type {
  ComplexDetail,
  ComplexListItem,
  PaginatedResponse,
  PriceSummary,
  Region,
  Rent,
  RentChartData,
  Trade,
  TradeChartData,
  UnitType,
} from '@/types';

// === 지역 ===
const SIDO_LIST: Region[] = [
  { name: '서울특별시', code: '11' },
  { name: '경기도', code: '41' },
  { name: '부산광역시', code: '26' },
];

const SIGUNGU_MAP: Record<string, Region[]> = {
  '11': [
    { name: '강남구', code: '11680' },
    { name: '서초구', code: '11650' },
    { name: '송파구', code: '11710' },
  ],
  '41': [
    { name: '성남시 분당구', code: '41135' },
    { name: '수원시 영통구', code: '41117' },
  ],
  '26': [{ name: '해운대구', code: '26350' }],
};

const EUPMYEONDONG_MAP: Record<string, Region[]> = {
  '11680': [
    { name: '대치동', code: '1168010300' },
    { name: '삼성동', code: '1168010500' },
    { name: '역삼동', code: '1168010100' },
  ],
  '11650': [
    { name: '반포동', code: '1165010100' },
    { name: '서초동', code: '1165010300' },
  ],
  '11710': [
    { name: '잠실동', code: '1171010100' },
    { name: '신천동', code: '1171010300' },
  ],
};

// === 단지 목록 ===
const COMPLEXES: ComplexListItem[] = [
  {
    complexId: 1,
    name: '래미안 대치 팰리스',
    builtYear: 2015,
    totalHousehold: 1608,
    eupmyeondongName: '대치동',
    addressShort: '서울 강남구 대치동',
    totalTradeCount: 523,
    recent3YearsTradeCount: 87,
  },
  {
    complexId: 2,
    name: '은마아파트',
    builtYear: 1979,
    totalHousehold: 4424,
    eupmyeondongName: '대치동',
    addressShort: '서울 강남구 대치동',
    totalTradeCount: 2341,
    recent3YearsTradeCount: 156,
  },
  {
    complexId: 3,
    name: '삼성 래미안',
    builtYear: 2009,
    totalHousehold: 720,
    eupmyeondongName: '삼성동',
    addressShort: '서울 강남구 삼성동',
    totalTradeCount: 312,
    recent3YearsTradeCount: 45,
  },
];

// === 단지 상세 ===
const COMPLEX_DETAILS: Record<number, ComplexDetail> = {
  1: {
    id: 1,
    name: '래미안 대치 팰리스',
    addressRoad: '서울특별시 강남구 삼성로 212',
    addressJibun: '서울특별시 강남구 대치동 316',
    builtYear: 2015,
    totalHousehold: 1608,
    totalBuilding: 16,
    totalParking: 2130,
    floorAreaRatio: 249,
    buildingCoverageRatio: 18,
    heatingType: '지역난방',
    isSubscribed: false,
  },
  2: {
    id: 2,
    name: '은마아파트',
    addressRoad: '서울특별시 강남구 삼성로 118',
    addressJibun: '서울특별시 강남구 대치동 937',
    builtYear: 1979,
    totalHousehold: 4424,
    totalBuilding: 28,
    totalParking: 2200,
    floorAreaRatio: 169,
    buildingCoverageRatio: 17,
    heatingType: '개별난방',
    isSubscribed: false,
  },
  3: {
    id: 3,
    name: '삼성 래미안',
    addressRoad: '서울특별시 강남구 삼성로 150',
    addressJibun: '서울특별시 강남구 삼성동 168',
    builtYear: 2009,
    totalHousehold: 720,
    totalBuilding: 8,
    totalParking: 980,
    floorAreaRatio: 220,
    buildingCoverageRatio: 15,
    heatingType: '지역난방',
    isSubscribed: false,
  },
};

// === 평형 ===
const UNIT_TYPES_MAP: Record<number, UnitType[]> = {
  1: [
    { unitTypeId: 101, exclusivePyeong: 24 },
    { unitTypeId: 102, exclusivePyeong: 34 },
    { unitTypeId: 103, exclusivePyeong: 49 },
  ],
  2: [
    { unitTypeId: 201, exclusivePyeong: 28 },
    { unitTypeId: 202, exclusivePyeong: 34 },
  ],
  3: [
    { unitTypeId: 301, exclusivePyeong: 33 },
    { unitTypeId: 302, exclusivePyeong: 45 },
    { unitTypeId: 303, exclusivePyeong: 59 },
  ],
};

// === 매매 거래 ===
const TRADES: Trade[] = [
  {
    tradeId: 1,
    contractDate: '2025-12-15',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 12,
    price: 285000,
    isDirect: false,
    isCanceled: false,
  },
  {
    tradeId: 2,
    contractDate: '2025-11-20',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 8,
    price: 278000,
    isDirect: false,
    isCanceled: false,
  },
  {
    tradeId: 3,
    contractDate: '2025-10-05',
    exclusiveArea: 59.97,
    exclusivePyeong: 24,
    floor: 15,
    price: 215000,
    isDirect: true,
    isCanceled: false,
  },
  {
    tradeId: 4,
    contractDate: '2025-09-18',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 3,
    price: 270000,
    isDirect: false,
    isCanceled: true,
  },
  {
    tradeId: 5,
    contractDate: '2025-08-22',
    exclusiveArea: 131.97,
    exclusivePyeong: 49,
    floor: 20,
    price: 420000,
    isDirect: false,
    isCanceled: false,
  },
  {
    tradeId: 6,
    contractDate: '2025-07-10',
    exclusiveArea: 59.97,
    exclusivePyeong: 24,
    floor: 5,
    price: 208000,
    isDirect: false,
    isCanceled: false,
  },
  {
    tradeId: 7,
    contractDate: '2025-06-03',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 18,
    price: 282000,
    isDirect: false,
    isCanceled: false,
  },
];

// === 전월세 거래 ===
const RENTS: Rent[] = [
  {
    rentId: 1,
    contractDate: '2025-12-10',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 7,
    rentType: 'JEONSE',
    deposit: 125000,
    monthlyRent: 0,
    isCanceled: false,
  },
  {
    rentId: 2,
    contractDate: '2025-11-25',
    exclusiveArea: 59.97,
    exclusivePyeong: 24,
    floor: 10,
    rentType: 'JEONSE',
    deposit: 95000,
    monthlyRent: 0,
    isCanceled: false,
  },
  {
    rentId: 3,
    contractDate: '2025-11-01',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 14,
    rentType: 'MONTHLY_RENT',
    deposit: 50000,
    monthlyRent: 150,
    isCanceled: false,
  },
  {
    rentId: 4,
    contractDate: '2025-10-12',
    exclusiveArea: 84.95,
    exclusivePyeong: 34,
    floor: 2,
    rentType: 'JEONSE',
    deposit: 120000,
    monthlyRent: 0,
    isCanceled: true,
  },
  {
    rentId: 5,
    contractDate: '2025-09-05',
    exclusiveArea: 131.97,
    exclusivePyeong: 49,
    floor: 16,
    rentType: 'JEONSE',
    deposit: 180000,
    monthlyRent: 0,
    isCanceled: false,
  },
];

// === 차트 데이터 ===
const TRADE_CHART_DATA: TradeChartData = {
  priceChart: [
    { yearMonth: '2024-01', avgPrice: 250000, maxPrice: 270000, minPrice: 230000 },
    { yearMonth: '2024-03', avgPrice: 255000, maxPrice: 275000, minPrice: 240000 },
    { yearMonth: '2024-05', avgPrice: 260000, maxPrice: 280000, minPrice: 245000 },
    { yearMonth: '2024-07', avgPrice: 258000, maxPrice: 278000, minPrice: 242000 },
    { yearMonth: '2024-09', avgPrice: 265000, maxPrice: 285000, minPrice: 250000 },
    { yearMonth: '2024-11', avgPrice: 270000, maxPrice: 290000, minPrice: 255000 },
    { yearMonth: '2025-01', avgPrice: 268000, maxPrice: 288000, minPrice: 252000 },
    { yearMonth: '2025-03', avgPrice: 275000, maxPrice: 295000, minPrice: 260000 },
    { yearMonth: '2025-05', avgPrice: 272000, maxPrice: 292000, minPrice: 258000 },
    { yearMonth: '2025-07', avgPrice: 278000, maxPrice: 298000, minPrice: 262000 },
    { yearMonth: '2025-09', avgPrice: 280000, maxPrice: 300000, minPrice: 265000 },
    { yearMonth: '2025-11', avgPrice: 285000, maxPrice: 305000, minPrice: 268000 },
  ],
  volumeChart: [
    { yearMonth: '2024-01', tradeCount: 3, rentCount: 5 },
    { yearMonth: '2024-03', tradeCount: 5, rentCount: 4 },
    { yearMonth: '2024-05', tradeCount: 2, rentCount: 6 },
    { yearMonth: '2024-07', tradeCount: 4, rentCount: 3 },
    { yearMonth: '2024-09', tradeCount: 6, rentCount: 7 },
    { yearMonth: '2024-11', tradeCount: 3, rentCount: 4 },
    { yearMonth: '2025-01', tradeCount: 2, rentCount: 5 },
    { yearMonth: '2025-03', tradeCount: 7, rentCount: 3 },
    { yearMonth: '2025-05', tradeCount: 4, rentCount: 6 },
    { yearMonth: '2025-07', tradeCount: 5, rentCount: 4 },
    { yearMonth: '2025-09', tradeCount: 3, rentCount: 5 },
    { yearMonth: '2025-11', tradeCount: 6, rentCount: 7 },
  ],
};

const RENT_CHART_DATA: RentChartData = {
  priceChart: [
    { yearMonth: '2024-01', avgDeposit: 95000, maxDeposit: 110000, minDeposit: 85000 },
    { yearMonth: '2024-03', avgDeposit: 97000, maxDeposit: 112000, minDeposit: 87000 },
    { yearMonth: '2024-05', avgDeposit: 100000, maxDeposit: 115000, minDeposit: 90000 },
    { yearMonth: '2024-07', avgDeposit: 98000, maxDeposit: 113000, minDeposit: 88000 },
    { yearMonth: '2024-09', avgDeposit: 102000, maxDeposit: 118000, minDeposit: 92000 },
    { yearMonth: '2024-11', avgDeposit: 105000, maxDeposit: 120000, minDeposit: 95000 },
    { yearMonth: '2025-01', avgDeposit: 103000, maxDeposit: 118000, minDeposit: 93000 },
    { yearMonth: '2025-03', avgDeposit: 108000, maxDeposit: 123000, minDeposit: 98000 },
    { yearMonth: '2025-05', avgDeposit: 106000, maxDeposit: 121000, minDeposit: 96000 },
    { yearMonth: '2025-07', avgDeposit: 110000, maxDeposit: 125000, minDeposit: 100000 },
    { yearMonth: '2025-09', avgDeposit: 112000, maxDeposit: 128000, minDeposit: 102000 },
    { yearMonth: '2025-11', avgDeposit: 115000, maxDeposit: 130000, minDeposit: 105000 },
  ],
  volumeChart: [
    { yearMonth: '2024-01', tradeCount: 3, rentCount: 5 },
    { yearMonth: '2024-03', tradeCount: 5, rentCount: 4 },
    { yearMonth: '2024-05', tradeCount: 2, rentCount: 6 },
    { yearMonth: '2024-07', tradeCount: 4, rentCount: 3 },
    { yearMonth: '2024-09', tradeCount: 6, rentCount: 7 },
    { yearMonth: '2024-11', tradeCount: 3, rentCount: 4 },
    { yearMonth: '2025-01', tradeCount: 2, rentCount: 5 },
    { yearMonth: '2025-03', tradeCount: 7, rentCount: 3 },
    { yearMonth: '2025-05', tradeCount: 4, rentCount: 6 },
    { yearMonth: '2025-07', tradeCount: 5, rentCount: 4 },
    { yearMonth: '2025-09', tradeCount: 3, rentCount: 5 },
    { yearMonth: '2025-11', tradeCount: 6, rentCount: 7 },
  ],
};

// === 가격 요약 ===
// lookbackMonths에 따라 비교 가격과 변동률이 달라지는 mock
function generatePriceSummary(lookbackMonths: number): PriceSummary {
  // 기준: 최근 1개월 매매 평균 28.5억, 전세 평균 12.5억
  const recentTradePrice = 285000;
  const recentRentDeposit = 125000;

  // lookback이 클수록 과거 가격이 낮게 (상승 추세 시뮬레이션)
  const tradeChangeMap: Record<number, number> = {
    1: -0.5,
    3: 2.1,
    6: 4.8,
    12: 8.5,
    24: 15.2,
    36: 22.0,
    60: 35.5,
  };
  const rentChangeMap: Record<number, number> = {
    1: 0.3,
    3: 1.8,
    6: 3.5,
    12: 6.2,
    24: 12.0,
    36: 18.5,
    60: 28.0,
  };

  const tradeChangeRate = tradeChangeMap[lookbackMonths] ?? 5.0;
  const rentChangeRate = rentChangeMap[lookbackMonths] ?? 3.0;

  const lookbackTradePrice = Math.round(recentTradePrice / (1 + tradeChangeRate / 100));
  const lookbackRentDeposit = Math.round(recentRentDeposit / (1 + rentChangeRate / 100));

  return {
    lookbackMonths,
    trade: {
      recentMonthAvgPrice: recentTradePrice,
      lookbackMonthAvgPrice: lookbackTradePrice,
      priceChangeRate: tradeChangeRate,
    },
    rent: {
      recentMonthAvgDeposit: recentRentDeposit,
      lookbackMonthAvgDeposit: lookbackRentDeposit,
      depositChangeRate: rentChangeRate,
    },
  };
}

// === Mock 라우터 ===
// URL 패턴을 보고 적절한 mock 데이터를 반환
export function getMockData(path: string): unknown {
  // 지역
  if (path === '/v1/apartments/regions/sido') {
    return SIDO_LIST;
  }
  if (path.startsWith('/v1/apartments/regions/sigungu')) {
    const code = new URL(`http://x${path}`).searchParams.get('sidoCode') ?? '';
    return SIGUNGU_MAP[code] ?? [];
  }
  if (path.startsWith('/v1/apartments/regions/eupmyeondong')) {
    const code = new URL(`http://x${path}`).searchParams.get('sigunguCode') ?? '';
    return EUPMYEONDONG_MAP[code] ?? [];
  }

  // 단지 상세: /v1/apartments/complexes/{id} (뒤에 추가 경로 없음)
  const detailMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)$/);
  if (detailMatch) {
    const id = parseInt(detailMatch[1], 10);
    return COMPLEX_DETAILS[id] ?? null;
  }

  // 평형: /v1/apartments/complexes/{id}/unit-types
  const unitTypesMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)\/unit-types$/);
  if (unitTypesMatch) {
    const id = parseInt(unitTypesMatch[1], 10);
    return UNIT_TYPES_MAP[id] ?? [];
  }

  // 가격 요약: /v1/apartments/complexes/{id}/price-summary
  const priceSummaryMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)\/price-summary/);
  if (priceSummaryMatch) {
    const lookbackMonths = Number(
      new URL(`http://x${path}`).searchParams.get('lookbackMonths') ?? '3',
    );
    return generatePriceSummary(lookbackMonths);
  }

  // 매매 차트: /v1/apartments/complexes/{id}/trades/chart
  const tradeChartMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)\/trades\/chart/);
  if (tradeChartMatch) {
    return TRADE_CHART_DATA;
  }

  // 전세 차트: /v1/apartments/complexes/{id}/rents/chart
  const rentChartMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)\/rents\/chart/);
  if (rentChartMatch) {
    return RENT_CHART_DATA;
  }

  // 매매 거래: /v1/apartments/complexes/{id}/trades
  const tradesMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)\/trades/);
  if (tradesMatch) {
    const response: PaginatedResponse<Trade> = {
      content: TRADES,
      totalElements: TRADES.length,
      totalPages: 1,
    };
    return response;
  }

  // 전월세 거래: /v1/apartments/complexes/{id}/rents
  const rentsMatch = path.match(/^\/v1\/apartments\/complexes\/(\d+)\/rents/);
  if (rentsMatch) {
    const response: PaginatedResponse<Rent> = {
      content: RENTS,
      totalElements: RENTS.length,
      totalPages: 1,
    };
    return response;
  }

  // 단지 목록: /v1/apartments/complexes?...
  if (path.startsWith('/v1/apartments/complexes')) {
    const response: PaginatedResponse<ComplexListItem> = {
      content: COMPLEXES,
      totalElements: COMPLEXES.length,
      totalPages: 1,
    };
    return response;
  }

  return null;
}

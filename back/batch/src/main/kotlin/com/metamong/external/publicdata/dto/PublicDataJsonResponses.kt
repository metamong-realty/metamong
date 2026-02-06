package com.metamong.external.publicdata.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 공공데이터 JSON API 공통 응답 구조
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicDataJsonResponse<T>(
    val response: JsonResponseBody<T>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonResponseBody<T>(
    val header: JsonResponseHeader? = null,
    val body: JsonResponseContent<T>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonResponseHeader(
    val resultCode: String? = null,
    val resultMsg: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonResponseContent<T>(
    val items: JsonItems<T>? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonItems<T>(
    val item: List<T>? = null,
)

/**
 * items가 배열로 직접 오는 API 응답용 (공동주택 단지 목록 등)
 * 구조: { "response": { "body": { "items": [...] } } }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicDataJsonListResponse<T>(
    val response: JsonListResponseBody<T>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonListResponseBody<T>(
    val header: JsonResponseHeader? = null,
    val body: JsonListResponseContent<T>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonListResponseContent<T>(
    val items: List<T>? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null,
)

/**
 * 단일 아이템 응답용 (공동주택 기본 정보 등)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicDataJsonSingleResponse<T>(
    val response: JsonSingleResponseBody<T>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonSingleResponseBody<T>(
    val header: JsonResponseHeader? = null,
    val body: JsonSingleResponseContent<T>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonSingleResponseContent<T>(
    val item: T? = null,
)

/**
 * 아파트 매매 실거래가 JSON 아이템
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentTradeJsonItem(
    val dealYear: String? = null,
    val dealMonth: String? = null,
    val dealDay: String? = null,
    val aptNm: String? = null,
    val aptSeq: String? = null,
    val excluUseAr: String? = null,
    val dealAmount: String? = null,
    val floor: String? = null,
    val buildYear: String? = null,
    val roadNm: String? = null,
    val roadNmBonbun: String? = null,
    val roadNmBubun: String? = null,
    val umdNm: String? = null,
    val jibun: String? = null,
    val cdealType: String? = null,
    val cdealDay: String? = null,
    val dealingGbn: String? = null,
    val rgstDate: String? = null,
    val buyerGbn: String? = null,
    val slerGbn: String? = null,
    val estateAgentSggNm: String? = null,
)

/**
 * 아파트 전월세 실거래가 JSON 아이템
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentRentJsonItem(
    val dealYear: String? = null,
    val dealMonth: String? = null,
    val dealDay: String? = null,
    val aptNm: String? = null,
    val aptSeq: String? = null,
    val excluUseAr: String? = null,
    val deposit: String? = null,
    val monthlyRent: String? = null,
    val floor: String? = null,
    val buildYear: String? = null,
    val roadnm: String? = null,
    val umdNm: String? = null,
    val jibun: String? = null,
    val sggCd: String? = null,
    val contractType: String? = null,
    val contractTerm: String? = null,
    val preDeposit: String? = null,
    val preMonthlyRent: String? = null,
    val useRRRight: String? = null,
)

/**
 * 공동주택 단지 목록 JSON 아이템
 * API: 국토교통부 공동주택단지 목록제공 서비스
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexListJsonItem(
    val kaptCode: String? = null, // 단지코드 (예: "A10021295")
    val kaptName: String? = null, // 단지명 (예: "경희궁의아침4단지")
    val bjdCode: String? = null, // 법정동코드 (예: "1111011800")
    val as1: String? = null, // 시도명 (예: "서울특별시")
    val as2: String? = null, // 시군구명 (예: "종로구")
    val as3: String? = null, // 읍면동명 (예: "내수동")
    val as4: String? = null, // 리명 (대부분 null)
)

/**
 * 공동주택 기본 정보 JSON 아이템
 * API: 국토교통부 공동주택단지 기본정보 서비스
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexInfoJsonItem(
    val kaptCode: String? = null, // 단지코드
    val kaptName: String? = null, // 단지명
    val kaptAddr: String? = null, // 단지주소
    val doroJuso: String? = null, // 도로명주소
    val codeSaleNm: String? = null, // 분양구분 (분양/임대)
    val codeHeatNm: String? = null, // 난방방식
    val codeHallNm: String? = null, // 복도유형 (계단식/복도식)
    val codeMgrNm: String? = null, // 관리방식 (위탁관리/자치관리)
    val codeAptNm: String? = null, // 주택유형 (아파트)
    val kaptTarea: Double? = null, // 단지전용면적 (㎡)
    val kaptMarea: Double? = null, // 단지관리비부과면적 (㎡)
    val kaptDongCnt: String? = null, // 동수
    val hoCnt: Int? = null, // 세대수
    val kaptdaCnt: Double? = null, // 총 주차대수
    val kaptTopFloor: Int? = null, // 최고층
    val kaptBaseFloor: Int? = null, // 지하층수
    val ktownFlrNo: Int? = null, // 최고층수 (타운)
    val kaptMparea60: Double? = null, // 60㎡ 이하 세대수
    val kaptMparea85: Double? = null, // 60~85㎡ 세대수
    val kaptMparea135: Double? = null, // 85~135㎡ 세대수
    val kaptMparea136: Double? = null, // 135㎡ 초과 세대수
    val privArea: String? = null, // 전용면적합계
    val kaptBcompany: String? = null, // 시공사
    val kaptAcompany: String? = null, // 시행사
    val kaptTel: String? = null, // 관리사무소 전화번호
    val kaptFax: String? = null, // 관리사무소 팩스번호
    val kaptUrl: String? = null, // 홈페이지 URL
    val kaptUsedate: String? = null, // 사용승인일 (yyyyMMdd)
    val bjdCode: String? = null, // 법정동코드
    val kaptdEcntp: Int? = null, // 전기계약종별
    val zipcode: String? = null, // 우편번호
)

/**
 * 주택인허가 정보 응답 (response 래퍼 없음)
 * 구조: { "header": {...}, "body": { "items": { "item": [...] } } }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class HousingLicenseJsonResponse(
    val header: JsonResponseHeader? = null,
    val body: JsonResponseContent<HousingLicenseJsonItem>? = null,
)

/**
 * 주택인허가 동별개요 JSON 아이템
 * API: 국토교통부 주택인허가정보 서비스 - 동별개요
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class HousingLicenseJsonItem(
    val rnum: String? = null, // 순번
    val sigunguCd: String? = null, // 시군구코드
    val bjdongCd: String? = null, // 법정동코드
    val platGbCd: String? = null, // 대지구분코드 (0:대지, 1:산, 2:블록)
    val bun: String? = null, // 번
    val ji: String? = null, // 지
    val mgmDongOulnPk: String? = null, // 관리동별개요PK
    val mgmHsrgstPk: String? = null, // 관리주택대장PK
    val bldNm: String? = null, // 건물명
    val splotNm: String? = null, // 특수지명
    val block: String? = null, // 블록
    val lot: String? = null, // 로트
    val mainAtchGbCd: String? = null, // 주부속구분코드
    val mainAtchGbCdNm: String? = null, // 주부속구분코드명
    val dongNm: String? = null, // 동명칭
    val mainPurpsCd: String? = null, // 주용도코드
    val mainPurpsCdNm: String? = null, // 주용도코드명
    val hhldCntPeplRent: String? = null, // 세대수국민임대(세대)
    val hhldCntPubRent_5: String? = null, // 세대수공공임대5(세대)
    val hhldCntPubRent_10: String? = null, // 세대수공공임대10(세대)
    val hhldCntPubRentEtc: String? = null, // 세대수공공임대기타(세대)
    val hhldCntPubRentTot: String? = null, // 세대수공공임대계(세대)
    val hhldCntPubLotou: String? = null, // 세대수공공분양(세대)
    val hhldCntEmplRent: String? = null, // 세대수사원임대(세대)
    val hhldCntLaborWlfar: String? = null, // 세대수근로복지(세대)
    val hhldCntCvlRent: String? = null, // 세대수민간임대(세대)
    val hhldCntCvlLotou: String? = null, // 세대수민간분양(세대)
    val strctCd: String? = null, // 구조코드
    val strctCdNm: String? = null, // 구조코드명
    val roofCd: String? = null, // 지붕코드
    val roofCdNm: String? = null, // 지붕코드명
    val archArea: String? = null, // 건축면적(㎡)
    val totArea: String? = null, // 연면적(㎡)
    val ugrndArea: String? = null, // 지하면적(㎡)
    val vlRatEstmTotArea: String? = null, // 용적률산정연면적(㎡) < 핵심 데이터
    val ugrndFlrCnt: String? = null, // 지하층수
    val grndFlrCnt: String? = null, // 지상층수
    val heit: String? = null, // 높이(m)
    val rideUseElvtCnt: String? = null, // 승용승강기수
    val emgenUseElvtCnt: String? = null, // 비상용승강기수
    val flrhFrom: String? = null, // 층고FROM
    val ceilHeit: String? = null, // 반자높이(m)
    val stairValidWidth: String? = null, // 계단유효폭
    val hwayWidth: String? = null, // 복도너비
    val ouwlThick: String? = null, // 외벽두께
    val adjHhldWallThick: String? = null, // 인접세대벽두께
    val platPlc: String? = null, // 대지위치
    val crtnDay: String? = null, // 생성일자
)

/**
 * 파싱 결과를 담는 래퍼 클래스 (totalCount 포함)
 */
data class ParseResult<T>(
    val items: List<T>,
    val totalCount: Int,
    val pageNo: Int,
    val numOfRows: Int,
) {
    /**
     * 다음 페이지 존재 여부
     * - items가 비어있으면 더 이상 가져올 데이터 없음
     * - 현재까지 가져온 데이터 수(pageNo * numOfRows)가 totalCount보다 작으면 더 있음
     */
    val hasMore: Boolean
        get() = items.isNotEmpty() && pageNo * numOfRows < totalCount
}

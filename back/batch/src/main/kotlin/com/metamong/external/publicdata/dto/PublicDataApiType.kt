package com.metamong.external.publicdata.dto

/**
 * API 응답 형식
 */
enum class ResponseFormat {
    JSON,
}

/**
 * 공공데이터 API 타입
 */
enum class PublicDataApiType(
    val path: String,
    val description: String,
    val responseFormat: ResponseFormat,
) {
    // 아파트 매매 실거래가 자료
    APARTMENT_TRADE(
        path = "/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev",
        description = "아파트 매매 실거래가",
        responseFormat = ResponseFormat.JSON,
    ),

    // 아파트 전월세 실거래가 자료
    APARTMENT_RENT(
        path = "/1613000/RTMSDataSvcAptRent/getRTMSDataSvcAptRent",
        description = "아파트 전월세 실거래가",
        responseFormat = ResponseFormat.JSON,
    ),

    // 주택인허가정보
    HOUSING_LICENSE(
        path = "/1613000/HsPmsHubService/getHpDongOulnInfo",
        description = "주택인허가정보",
        responseFormat = ResponseFormat.JSON,
    ),

    // 공동주택 단지 목록
    APARTMENT_COMPLEX_LIST(
        path = "/1613000/AptListService3/getSigunguAptList3",
        description = "공동주택 단지 목록",
        responseFormat = ResponseFormat.JSON,
    ),

    // 공동주택 기본 정보
    APARTMENT_COMPLEX_INFO(
        path = "/1613000/AptBasisInfoServiceV4/getAphusBassInfoV4",
        description = "공동주택 기본 정보",
        responseFormat = ResponseFormat.JSON,
    ),
}

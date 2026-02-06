package com.metamong.external.publicdata.dto

/**
 * 지역코드 데이터
 * 대상 지역: 서울(25개), 경기(42개), 인천(10개), 부산(16개), 대구(9개), 광주(5개), 대전(5개), 울산(5개)
 */
data class RegionCode(
    val sidoCd: String,
    val sigunguCd: String,
    val sigunguNm: String,
    val bjdongCd: String? = null,
) {
    companion object {
        /**
         * 서울 + 수도권 + 5대 광역시 지역코드 목록
         */
        fun getTargetRegions(): List<RegionCode> =
            listOf(
                // 서울 (25개 구)
                RegionCode("11", "11110", "종로구"),
                RegionCode("11", "11140", "중구"),
                RegionCode("11", "11170", "용산구"),
                RegionCode("11", "11200", "성동구"),
                RegionCode("11", "11215", "광진구"),
                RegionCode("11", "11230", "동대문구"),
                RegionCode("11", "11260", "중랑구"),
                RegionCode("11", "11290", "성북구"),
                RegionCode("11", "11305", "강북구"),
                RegionCode("11", "11320", "도봉구"),
                RegionCode("11", "11350", "노원구"),
                RegionCode("11", "11380", "은평구"),
                RegionCode("11", "11410", "서대문구"),
                RegionCode("11", "11440", "마포구"),
                RegionCode("11", "11470", "양천구"),
                RegionCode("11", "11500", "강서구"),
                RegionCode("11", "11530", "구로구"),
                RegionCode("11", "11545", "금천구"),
                RegionCode("11", "11560", "영등포구"),
                RegionCode("11", "11590", "동작구"),
                RegionCode("11", "11620", "관악구"),
                RegionCode("11", "11650", "서초구"),
                RegionCode("11", "11680", "강남구"),
                RegionCode("11", "11710", "송파구"),
                RegionCode("11", "11740", "강동구"),
                // 경기 (39개 구역 - 28개 시의 구 단위 포함)
                RegionCode("41", "41111", "수원시 장안구"),
                RegionCode("41", "41113", "수원시 권선구"),
                RegionCode("41", "41115", "수원시 팔달구"),
                RegionCode("41", "41117", "수원시 영통구"),
                RegionCode("41", "41131", "성남시 수정구"),
                RegionCode("41", "41133", "성남시 중원구"),
                RegionCode("41", "41135", "성남시 분당구"),
                RegionCode("41", "41150", "의정부시"),
                RegionCode("41", "41171", "안양시 만안구"),
                RegionCode("41", "41173", "안양시 동안구"),
                RegionCode("41", "41190", "부천시"),
                RegionCode("41", "41210", "광명시"),
                RegionCode("41", "41220", "평택시"),
                RegionCode("41", "41250", "동두천시"),
                RegionCode("41", "41271", "안산시 상록구"),
                RegionCode("41", "41273", "안산시 단원구"),
                RegionCode("41", "41281", "고양시 덕양구"),
                RegionCode("41", "41285", "고양시 일산동구"),
                RegionCode("41", "41287", "고양시 일산서구"),
                RegionCode("41", "41290", "과천시"),
                RegionCode("41", "41310", "구리시"),
                RegionCode("41", "41360", "남양주시"),
                RegionCode("41", "41370", "오산시"),
                RegionCode("41", "41390", "시흥시"),
                RegionCode("41", "41410", "군포시"),
                RegionCode("41", "41430", "의왕시"),
                RegionCode("41", "41450", "하남시"),
                RegionCode("41", "41461", "용인시 처인구"),
                RegionCode("41", "41463", "용인시 기흥구"),
                RegionCode("41", "41465", "용인시 수지구"),
                RegionCode("41", "41480", "파주시"),
                RegionCode("41", "41500", "이천시"),
                RegionCode("41", "41550", "안성시"),
                RegionCode("41", "41570", "김포시"),
                RegionCode("41", "41590", "화성시"),
                RegionCode("41", "41610", "광주시"),
                RegionCode("41", "41630", "양주시"),
                RegionCode("41", "41650", "포천시"),
                RegionCode("41", "41670", "여주시"),
                // 인천 (10개 구군)
                RegionCode("28", "28110", "중구"),
                RegionCode("28", "28140", "동구"),
                RegionCode("28", "28177", "미추홀구"),
                RegionCode("28", "28185", "연수구"),
                RegionCode("28", "28200", "남동구"),
                RegionCode("28", "28237", "부평구"),
                RegionCode("28", "28245", "계양구"),
                RegionCode("28", "28260", "서구"),
                RegionCode("28", "28710", "강화군"),
                RegionCode("28", "28720", "옹진군"),
                // 부산 (16개 구군)
                RegionCode("26", "26110", "중구"),
                RegionCode("26", "26140", "서구"),
                RegionCode("26", "26170", "동구"),
                RegionCode("26", "26200", "영도구"),
                RegionCode("26", "26230", "부산진구"),
                RegionCode("26", "26260", "동래구"),
                RegionCode("26", "26290", "남구"),
                RegionCode("26", "26320", "북구"),
                RegionCode("26", "26350", "해운대구"),
                RegionCode("26", "26380", "사하구"),
                RegionCode("26", "26410", "금정구"),
                RegionCode("26", "26440", "강서구"),
                RegionCode("26", "26470", "연제구"),
                RegionCode("26", "26500", "수영구"),
                RegionCode("26", "26530", "사상구"),
                RegionCode("26", "26710", "기장군"),
                // 대구 (8개 구군)
                RegionCode("27", "27110", "중구"),
                RegionCode("27", "27140", "동구"),
                RegionCode("27", "27170", "서구"),
                RegionCode("27", "27200", "남구"),
                RegionCode("27", "27230", "북구"),
                RegionCode("27", "27260", "수성구"),
                RegionCode("27", "27290", "달서구"),
                RegionCode("27", "27710", "달성군"),
                // 광주 (5개 구)
                RegionCode("29", "29110", "동구"),
                RegionCode("29", "29140", "서구"),
                RegionCode("29", "29155", "남구"),
                RegionCode("29", "29170", "북구"),
                RegionCode("29", "29200", "광산구"),
                // 대전 (5개 구)
                RegionCode("30", "30110", "동구"),
                RegionCode("30", "30140", "중구"),
                RegionCode("30", "30170", "서구"),
                RegionCode("30", "30200", "유성구"),
                RegionCode("30", "30230", "대덕구"),
                // 울산 (5개 구군)
                RegionCode("31", "31110", "중구"),
                RegionCode("31", "31140", "남구"),
                RegionCode("31", "31170", "동구"),
                RegionCode("31", "31200", "북구"),
                RegionCode("31", "31710", "울주군"),
            )
    }
}

/**
 * 지역코드 + 연월 조합 데이터
 * Historical Job에서 여러 월을 한 번에 처리할 때 사용
 */
data class RegionCodeWithYearMonth(
    val regionCode: RegionCode,
    val yearMonth: String,
)

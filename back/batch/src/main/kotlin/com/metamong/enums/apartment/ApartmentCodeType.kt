package com.metamong.enums.apartment

enum class ApartmentCodeType(
    val description: String,
) {
    APT_SEQ("실거래가 API 단지 식별자"),
    KAPT_CODE("공동주택 단지정보 API 식별자"),
    LICENSE_PK("건축 인허가 API 식별자"),
}

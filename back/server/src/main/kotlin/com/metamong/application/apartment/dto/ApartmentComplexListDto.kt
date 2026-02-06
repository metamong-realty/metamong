package com.metamong.application.apartment.dto

import com.metamong.infra.persistence.apartment.projection.ApartmentComplexListProjection

data class ApartmentComplexListDto(
    val complexId: Long,
    val name: String,
    val builtYear: Int?,
    val totalHousehold: Int?,
    val eupmyeondongName: String?,
    val addressShort: String?,
    val totalTradeCount: Long,
    val recent3YearsTradeCount: Long,
) {
    companion object {
        fun from(
            projection: ApartmentComplexListProjection,
            eupmyeondongName: String?,
            totalTradeCount: Long,
            recent3YearsTradeCount: Long,
        ) = ApartmentComplexListDto(
            complexId = projection.id,
            name = projection.name,
            builtYear = projection.builtYear?.toInt(),
            totalHousehold = projection.totalHousehold,
            eupmyeondongName = eupmyeondongName,
            addressShort = projection.addressJibun?.let { extractShortAddress(it) },
            totalTradeCount = totalTradeCount,
            recent3YearsTradeCount = recent3YearsTradeCount,
        )

        /**
         * 지번주소에서 짧은 주소 추출 (읍면동 + 번지)
         * 예: "서울특별시 강남구 역삼동 123-45" -> "역삼동 123-45"
         */
        private fun extractShortAddress(addressJibun: String): String {
            val parts = addressJibun.split(" ")
            // 읍/면/동 + 번지 추출 (끝에서 2개 또는 3개)
            return if (parts.size >= 2) {
                parts.takeLast(2).joinToString(" ")
            } else {
                addressJibun
            }
        }
    }
}
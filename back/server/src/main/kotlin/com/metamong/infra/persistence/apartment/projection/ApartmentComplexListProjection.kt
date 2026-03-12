package com.metamong.infra.persistence.apartment.projection

data class ApartmentComplexListProjection(
    val id: Long,
    val name: String,
    val builtYear: Int?,
    val totalHousehold: Int?,
    val eupmyeondongRiCode: Int?,
    val addressJibun: String?,
    val totalTradeCount: Long,
    val recent3YearsTradeCount: Long,
)

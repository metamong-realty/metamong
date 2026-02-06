package com.metamong.infra.persistence.apartment.projection

data class ApartmentComplexListProjection(
    val id: Long,
    val name: String,
    val builtYear: Short?,
    val totalHousehold: Int?,
    val eupmyeondongRiCode: Int?,
    val addressJibun: String?,
)

package com.metamong.application.apartment.dto

import java.math.BigDecimal

data class ApartmentComplexDetailDto(
    val id: Long,
    val name: String,
    val addressRoad: String?,
    val addressJibun: String?,
    val builtYear: Short?,
    val totalHousehold: Int?,
    val totalBuilding: Int?,
    val totalParking: Int?,
    val floorAreaRatio: BigDecimal?,
    val buildingCoverageRatio: BigDecimal?,
    val heatingType: String?,
    val isSubscribed: Boolean = false,
)
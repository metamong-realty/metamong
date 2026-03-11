package com.metamong.infra.persistence.apartment.projection

data class ApartmentRentChartProjection(
    val contractYear: Int,
    val contractMonth: Int,
    val avgDeposit: Double,
    val maxDeposit: Int,
    val minDeposit: Int,
    val rentCount: Long,
)

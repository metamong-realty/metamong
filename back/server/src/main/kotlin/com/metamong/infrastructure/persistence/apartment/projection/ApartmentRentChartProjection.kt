package com.metamong.infrastructure.persistence.apartment.projection

data class ApartmentRentChartProjection(
    val contractYear: Short,
    val contractMonth: Short,
    val avgDeposit: Double,
    val maxDeposit: Int,
    val minDeposit: Int,
    val rentCount: Long,
)
package com.metamong.infra.persistence.apartment.projection

data class ApartmentTradeChartProjection(
    val contractYear: Int,
    val contractMonth: Int,
    val avgPrice: Double,
    val maxPrice: Int,
    val minPrice: Int,
    val tradeCount: Long,
)

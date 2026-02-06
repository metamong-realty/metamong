package com.metamong.infra.persistence.apartment.projection

data class ApartmentTradeChartProjection(
    val contractYear: Short,
    val contractMonth: Short,
    val avgPrice: Double,
    val maxPrice: Int,
    val minPrice: Int,
    val tradeCount: Long,
)

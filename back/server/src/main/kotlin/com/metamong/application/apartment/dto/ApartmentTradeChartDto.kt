package com.metamong.application.apartment.dto

import com.metamong.infra.persistence.apartment.projection.ApartmentTradeChartProjection

data class ApartmentTradeChartDto(
    val yearMonth: String,
    val avgPrice: Long,
    val maxPrice: Int,
    val minPrice: Int,
    val tradeCount: Long,
) {
    companion object {
        fun from(projection: ApartmentTradeChartProjection) =
            ApartmentTradeChartDto(
                yearMonth = "${projection.contractYear}-${projection.contractMonth.toString().padStart(2, '0')}",
                avgPrice = projection.avgPrice.toLong(),
                maxPrice = projection.maxPrice,
                minPrice = projection.minPrice,
                tradeCount = projection.tradeCount,
            )
    }
}

package com.metamong.application.apartment.dto

import com.metamong.infra.persistence.apartment.projection.ApartmentRentChartProjection

data class ApartmentRentChartDto(
    val yearMonth: String,
    val avgDeposit: Long,
    val maxDeposit: Int,
    val minDeposit: Int,
    val rentCount: Long,
) {
    companion object {
        fun from(projection: ApartmentRentChartProjection) =
            ApartmentRentChartDto(
                yearMonth = "${projection.contractYear}-${projection.contractMonth.toString().padStart(2, '0')}",
                avgDeposit = projection.avgDeposit.toLong(),
                maxDeposit = projection.maxDeposit,
                minDeposit = projection.minDeposit,
                rentCount = projection.rentCount,
            )
    }
}
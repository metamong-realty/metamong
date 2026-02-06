package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentRentChartDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "아파트 전월세 차트 응답")
data class ApartmentRentChartResponse(
    @Schema(description = "보증금 차트 데이터")
    val priceChart: List<RentPriceChartItem>,
    @Schema(description = "거래량 차트 데이터")
    val volumeChart: List<RentVolumeChartItem>,
) {
    companion object {
        fun from(
            rentDtos: List<ApartmentRentChartDto>,
            tradeCount: Map<String, Long>,
        ) = ApartmentRentChartResponse(
            priceChart =
                rentDtos.map { dto ->
                    RentPriceChartItem(
                        yearMonth = dto.yearMonth,
                        avgDeposit = dto.avgDeposit,
                        maxDeposit = dto.maxDeposit,
                        minDeposit = dto.minDeposit,
                    )
                },
            volumeChart =
                rentDtos.map { dto ->
                    RentVolumeChartItem(
                        yearMonth = dto.yearMonth,
                        tradeCount = tradeCount[dto.yearMonth] ?: 0L,
                        rentCount = dto.rentCount,
                    )
                },
        )
    }
}

@Schema(description = "보증금 차트 항목")
data class RentPriceChartItem(
    @Schema(description = "연월", example = "2023-02")
    val yearMonth: String,
    @Schema(description = "평균 보증금 (만원)", example = "50000")
    val avgDeposit: Long,
    @Schema(description = "최고 보증금 (만원)", example = "55000")
    val maxDeposit: Int,
    @Schema(description = "최저 보증금 (만원)", example = "45000")
    val minDeposit: Int,
)

@Schema(description = "전월세 거래량 차트 항목")
data class RentVolumeChartItem(
    @Schema(description = "연월", example = "2023-02")
    val yearMonth: String,
    @Schema(description = "매매 거래건수", example = "2")
    val tradeCount: Long,
    @Schema(description = "전월세 거래건수", example = "3")
    val rentCount: Long,
)

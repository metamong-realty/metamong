package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentTradeChartDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "아파트 매매 차트 응답")
data class ApartmentTradeChartResponse(
    @Schema(description = "가격 차트 데이터")
    val priceChart: List<PriceChartItem>,
    @Schema(description = "거래량 차트 데이터")
    val volumeChart: List<VolumeChartItem>,
) {
    companion object {
        fun from(
            tradeDtos: List<ApartmentTradeChartDto>,
            rentCountMap: Map<String, Long>,
        ) = ApartmentTradeChartResponse(
            priceChart =
                tradeDtos.map { dto ->
                    PriceChartItem(
                        yearMonth = dto.yearMonth,
                        avgPrice = dto.avgPrice,
                        maxPrice = dto.maxPrice,
                        minPrice = dto.minPrice,
                    )
                },
            volumeChart =
                tradeDtos.map { dto ->
                    VolumeChartItem(
                        yearMonth = dto.yearMonth,
                        tradeCount = dto.tradeCount,
                        rentCount = rentCountMap[dto.yearMonth] ?: 0L,
                    )
                },
        )
    }
}

@Schema(description = "가격 차트 항목")
data class PriceChartItem(
    @Schema(description = "연월", example = "2023-02")
    val yearMonth: String,
    @Schema(description = "평균가격 (만원)", example = "55000")
    val avgPrice: Long,
    @Schema(description = "최고가격 (만원)", example = "60000")
    val maxPrice: Int,
    @Schema(description = "최저가격 (만원)", example = "50000")
    val minPrice: Int,
)

@Schema(description = "거래량 차트 항목")
data class VolumeChartItem(
    @Schema(description = "연월", example = "2023-02")
    val yearMonth: String,
    @Schema(description = "매매 거래건수", example = "3")
    val tradeCount: Long,
    @Schema(description = "전월세 거래건수", example = "1")
    val rentCount: Long,
)

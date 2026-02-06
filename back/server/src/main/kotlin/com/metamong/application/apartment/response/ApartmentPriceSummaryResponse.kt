package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentPriceSummaryDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "아파트 가격 요약 응답")
data class ApartmentPriceSummaryResponse(
    @Schema(description = "조회 기간 (개월)", example = "3")
    val lookbackMonths: Int,
    @Schema(description = "매매 가격 요약")
    val trade: TradePriceSummary?,
    @Schema(description = "전세 가격 요약")
    val rent: RentPriceSummary?,
) {
    companion object {
        fun of(
            dto: ApartmentPriceSummaryDto,
            lookbackMonths: Int,
        ) = ApartmentPriceSummaryResponse(
            lookbackMonths = lookbackMonths,
            trade =
                dto.trade?.let {
                    TradePriceSummary(
                        recentMonthAvgPrice = it.recentMonthAvgPrice,
                        lookbackMonthAvgPrice = it.lookbackMonthAvgPrice,
                        priceChangeRate = it.priceChangeRate,
                    )
                },
            rent =
                dto.rent?.let {
                    RentPriceSummary(
                        recentMonthAvgDeposit = it.recentMonthAvgDeposit,
                        lookbackMonthAvgDeposit = it.lookbackMonthAvgDeposit,
                        depositChangeRate = it.depositChangeRate,
                    )
                },
        )
    }
}

@Schema(description = "매매 가격 요약")
data class TradePriceSummary(
    @Schema(description = "최근 1개월 평균 매매가 (만원)", example = "120000")
    val recentMonthAvgPrice: Long,
    @Schema(description = "N개월 전 1개월간 평균 매매가 (만원)", example = "117000")
    val lookbackMonthAvgPrice: Long,
    @Schema(description = "N개월 전 평균가 대비 최신 평균가의 변동률 (%)", example = "2.5")
    val priceChangeRate: BigDecimal?,
)

@Schema(description = "전세 가격 요약")
data class RentPriceSummary(
    @Schema(description = "최근 1개월 평균 전세가 (만원)", example = "80000")
    val recentMonthAvgDeposit: Long,
    @Schema(description = "N개월 전 1개월간 평균 전세가 (만원)", example = "81000")
    val lookbackMonthAvgDeposit: Long,
    @Schema(description = "N개월 전 평균가 대비 최신 평균가의 변동률 (%)", example = "-1.2")
    val depositChangeRate: BigDecimal?,
)

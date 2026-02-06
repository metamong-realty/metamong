package com.metamong.application.apartment.dto

import java.math.BigDecimal

data class ApartmentPriceSummaryDto(
    val trade: TradePriceSummaryDto?,
    val rent: RentPriceSummaryDto?,
)

data class TradePriceSummaryDto(
    val recentMonthAvgPrice: Long,
    val lookbackMonthAvgPrice: Long,
    val priceChangeRate: BigDecimal?,
)

data class RentPriceSummaryDto(
    val recentMonthAvgDeposit: Long,
    val lookbackMonthAvgDeposit: Long,
    val depositChangeRate: BigDecimal?,
)

package com.metamong.application.apartment.request

import io.swagger.v3.oas.annotations.media.Schema

data class ApartmentTradeSearchRequest(
    @Schema(description = "평형 ID", example = "1")
    val unitTypeId: Long? = null,
    @Schema(description = "기간 (RECENT_3Y, ALL)", example = "RECENT_3Y")
    val period: PeriodType = PeriodType.RECENT_3YEARS,
)

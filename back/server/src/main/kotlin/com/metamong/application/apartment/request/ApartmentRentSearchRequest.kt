package com.metamong.application.apartment.request

import com.metamong.domain.apartment.model.RentType
import io.swagger.v3.oas.annotations.media.Schema

data class ApartmentRentSearchRequest(
    @Schema(description = "평형 ID", example = "1")
    val unitTypeId: Long? = null,
    @Schema(description = "전월세 구분 (JEONSE, MONTHLY)", example = "JEONSE")
    val rentType: RentType? = null,
    @Schema(description = "기간 (RECENT_3YEARS, ALL)", example = "RECENT_3YEARS")
    val period: PeriodType = PeriodType.RECENT_3YEARS,
)

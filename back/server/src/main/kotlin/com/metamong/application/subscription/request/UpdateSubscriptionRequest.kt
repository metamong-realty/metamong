package com.metamong.application.subscription.request

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class UpdateSubscriptionRequest(
    @Schema(description = "지역코드", example = "1168010100")
    val regionCode: String? = null,
    @Schema(description = "전용면적 ㎡", example = "84.99")
    val exclusiveArea: BigDecimal? = null,
    @Schema(description = "최소 가격", example = "50000")
    val minPrice: BigDecimal? = null,
    @Schema(description = "최대 가격", example = "100000")
    val maxPrice: BigDecimal? = null,
    @Schema(description = "활성 여부", example = "true")
    val isActive: Boolean? = null,
) {
    fun toDto(): UpdateSubscriptionRequestDto =
        UpdateSubscriptionRequestDto(
            regionCode = regionCode,
            exclusiveArea = exclusiveArea,
            minPrice = minPrice,
            maxPrice = maxPrice,
            isActive = isActive,
        )
}

data class UpdateSubscriptionRequestDto(
    val regionCode: String? = null,
    val exclusiveArea: BigDecimal? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val isActive: Boolean? = null,
)

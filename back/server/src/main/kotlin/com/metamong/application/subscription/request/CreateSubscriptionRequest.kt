package com.metamong.application.subscription.request

import com.metamong.domain.subscription.model.SubscriptionEntity
import com.metamong.domain.subscription.model.SubscriptionType
import com.metamong.domain.subscription.model.TradeType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateSubscriptionRequest(
    @field:NotNull(message = "구독 타입은 필수입니다")
    @Schema(description = "구독 타입", example = "COMPLEX")
    val type: SubscriptionType,
    @Schema(description = "거래 타입", example = "TRADE")
    val tradeType: TradeType = TradeType.TRADE,
    @Schema(description = "아파트 단지 ID (COMPLEX 타입 필수)", example = "1")
    val apartmentComplexId: Long? = null,
    @Schema(description = "지역코드 (REGION, CONDITION 타입 필수)", example = "1168010100")
    val regionCode: String? = null,
    @Schema(description = "전용면적 ㎡ (CONDITION 타입 선택)", example = "84.99")
    val exclusiveArea: BigDecimal? = null,
    @Schema(description = "최소 가격 (CONDITION 타입 선택)", example = "50000")
    val minPrice: BigDecimal? = null,
    @Schema(description = "최대 가격 (CONDITION 타입 선택)", example = "100000")
    val maxPrice: BigDecimal? = null,
) {
    fun toDto(): CreateSubscriptionRequestDto =
        CreateSubscriptionRequestDto(
            type = type,
            tradeType = tradeType,
            apartmentComplexId = apartmentComplexId,
            regionCode = regionCode,
            exclusiveArea = exclusiveArea,
            minPrice = minPrice,
            maxPrice = maxPrice,
        )
}

data class CreateSubscriptionRequestDto(
    val type: SubscriptionType,
    val tradeType: TradeType = TradeType.TRADE,
    val apartmentComplexId: Long? = null,
    val regionCode: String? = null,
    val exclusiveArea: BigDecimal? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
) {
    fun toEntity(userId: Long): SubscriptionEntity =
        SubscriptionEntity(
            userId = userId,
            type = type,
            tradeType = tradeType,
            apartmentComplexId = apartmentComplexId,
            regionCode = regionCode,
            exclusiveArea = exclusiveArea,
            minPrice = minPrice,
            maxPrice = maxPrice,
        )
}

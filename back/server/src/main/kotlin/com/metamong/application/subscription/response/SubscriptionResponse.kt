package com.metamong.application.subscription.response

import com.metamong.domain.subscription.model.SubscriptionEntity
import com.metamong.domain.subscription.model.SubscriptionType
import com.metamong.domain.subscription.model.TradeType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime

data class SubscriptionResponse(
    @Schema(description = "구독 ID", example = "1")
    val id: Long,
    @Schema(description = "구독 타입", example = "COMPLEX")
    val type: SubscriptionType,
    @Schema(description = "거래 타입", example = "TRADE")
    val tradeType: TradeType,
    @Schema(description = "아파트 단지 ID", example = "1")
    val apartmentComplexId: Long?,
    @Schema(description = "지역코드", example = "1168010100")
    val regionCode: String?,
    @Schema(description = "전용면적 ㎡", example = "84.99")
    val exclusiveArea: BigDecimal?,
    @Schema(description = "최소 가격", example = "50000")
    val minPrice: BigDecimal?,
    @Schema(description = "최대 가격", example = "100000")
    val maxPrice: BigDecimal?,
    @Schema(description = "활성 여부", example = "true")
    val isActive: Boolean,
    @Schema(description = "생성일시")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(entity: SubscriptionEntity): SubscriptionResponse =
            SubscriptionResponse(
                id = entity.id,
                type = entity.type,
                tradeType = entity.tradeType,
                apartmentComplexId = entity.apartmentComplexId,
                regionCode = entity.regionCode,
                exclusiveArea = entity.exclusiveArea,
                minPrice = entity.minPrice,
                maxPrice = entity.maxPrice,
                isActive = entity.isActive,
                createdAt = entity.createdAt,
            )
    }
}

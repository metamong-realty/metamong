package com.metamong.domain.subscription.model

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "subscriptions")
class SubscriptionEntity(
    val userId: Long,
    @Enumerated(EnumType.STRING)
    val type: SubscriptionType,
    @Enumerated(EnumType.STRING)
    val tradeType: TradeType = TradeType.TRADE,
    var apartmentComplexId: Long? = null,
    var regionCode: String? = null,
    var exclusiveArea: BigDecimal? = null,
    var minPrice: BigDecimal? = null,
    var maxPrice: BigDecimal? = null,
    var isActive: Boolean = true,
) : BaseEntity() {
    fun update(
        regionCode: String?,
        exclusiveArea: BigDecimal?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        isActive: Boolean?,
    ) {
        this.regionCode = regionCode
        this.exclusiveArea = exclusiveArea
        this.minPrice = minPrice
        this.maxPrice = maxPrice
        isActive?.let { this.isActive = it }
    }
}

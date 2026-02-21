package com.metamong.infra.persistence.subscription.repository

import com.metamong.domain.subscription.model.SubscriptionEntity
import java.math.BigDecimal

interface SubscriptionRepositoryCustom {
    fun findActiveByComplexId(complexId: Long): List<SubscriptionEntity>

    fun findActiveByRegionCodes(regionCodes: List<String>): List<SubscriptionEntity>

    fun findActiveConditionByRegionCodes(
        regionCodes: List<String>,
        exclusiveArea: BigDecimal?,
        price: BigDecimal?,
    ): List<SubscriptionEntity>
}

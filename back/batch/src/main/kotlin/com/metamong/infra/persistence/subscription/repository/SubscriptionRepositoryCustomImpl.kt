package com.metamong.infra.persistence.subscription.repository

import com.metamong.domain.subscription.model.QSubscriptionEntity
import com.metamong.domain.subscription.model.SubscriptionEntity
import com.metamong.domain.subscription.model.SubscriptionType
import com.metamong.support.QuerydslRepositorySupport
import com.querydsl.core.BooleanBuilder
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class SubscriptionRepositoryCustomImpl :
    QuerydslRepositorySupport(SubscriptionEntity::class.java),
    SubscriptionRepositoryCustom {
    private val subscription = QSubscriptionEntity.subscriptionEntity

    override fun findActiveByComplexId(complexId: Long): List<SubscriptionEntity> =
        queryFactory
            .selectFrom(subscription)
            .where(
                subscription.apartmentComplexId.eq(complexId),
                subscription.isActive.isTrue,
                subscription.type.eq(SubscriptionType.COMPLEX),
            ).fetch()

    override fun findActiveByRegionCodes(regionCodes: List<String>): List<SubscriptionEntity> =
        queryFactory
            .selectFrom(subscription)
            .where(
                subscription.regionCode.`in`(regionCodes),
                subscription.isActive.isTrue,
                subscription.type.eq(SubscriptionType.REGION),
            ).fetch()

    override fun findActiveConditionByRegionCodes(
        regionCodes: List<String>,
        exclusiveArea: BigDecimal?,
        price: BigDecimal?,
    ): List<SubscriptionEntity> {
        val builder =
            BooleanBuilder()
                .and(subscription.regionCode.`in`(regionCodes))
                .and(subscription.isActive.isTrue)
                .and(subscription.type.eq(SubscriptionType.CONDITION))

        exclusiveArea?.let {
            builder.and(
                subscription.exclusiveArea.isNull.or(subscription.exclusiveArea.eq(it)),
            )
        }

        price?.let { p ->
            builder.and(
                subscription.minPrice.isNull.or(subscription.minPrice.loe(p)),
            )
            builder.and(
                subscription.maxPrice.isNull.or(subscription.maxPrice.goe(p)),
            )
        }

        return queryFactory
            .selectFrom(subscription)
            .where(builder)
            .fetch()
    }
}

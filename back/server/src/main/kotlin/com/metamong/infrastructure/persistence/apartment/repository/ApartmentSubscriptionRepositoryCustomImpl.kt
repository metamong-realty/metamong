package com.metamong.infrastructure.persistence.apartment.repository

import com.metamong.support.QuerydslRepositorySupport
import com.metamong.domain.apartment.model.ApartmentSubscriptionEntity
import com.metamong.domain.apartment.model.QApartmentSubscriptionEntity
import org.springframework.stereotype.Repository

@Repository
class ApartmentSubscriptionRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentSubscriptionEntity::class.java),
    ApartmentSubscriptionRepositoryCustom {
    private val subscription = QApartmentSubscriptionEntity.apartmentSubscriptionEntity

    override fun findByUserId(userId: Long): List<ApartmentSubscriptionEntity> =
        queryFactory
            .selectFrom(subscription)
            .where(subscription.userId.eq(userId))
            .orderBy(subscription.createdAt.desc())
            .fetch()
}
package com.metamong.infra.persistence.subscription.repository

import com.metamong.domain.subscription.model.NotificationEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationEventRepository : JpaRepository<NotificationEventEntity, Long> {
    fun existsBySubscriptionIdAndTradeId(
        subscriptionId: Long,
        tradeId: Long,
    ): Boolean

    fun findAllByTradeIdIn(tradeIds: List<Long>): List<NotificationEventEntity>
}

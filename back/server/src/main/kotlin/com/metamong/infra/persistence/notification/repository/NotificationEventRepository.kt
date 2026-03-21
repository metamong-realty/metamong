package com.metamong.infra.persistence.notification.repository

import com.metamong.domain.subscription.model.NotificationEventEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationEventRepository :
    JpaRepository<NotificationEventEntity, Long>,
    NotificationEventRepositoryCustom {
    fun countByUserIdAndStatusIn(
        userId: Long,
        statuses: List<com.metamong.domain.subscription.model.NotificationStatus>,
    ): Long
}

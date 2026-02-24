package com.metamong.domain.subscription.model

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "notification_events")
class NotificationEventEntity(
    val userId: Long,
    val subscriptionId: Long,
    val tradeId: Long,
    @Enumerated(EnumType.STRING)
    var status: NotificationStatus = NotificationStatus.PENDING,
) : BaseEntity()

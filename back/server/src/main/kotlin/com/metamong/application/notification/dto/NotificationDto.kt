package com.metamong.application.notification.dto

import com.metamong.domain.subscription.model.NotificationStatus
import java.time.LocalDateTime

data class NotificationDto(
    val id: Long,
    val complexId: Long?,
    val complexName: String?,
    val exclusivePyeong: Int?,
    val price: Long?,
    val contractDate: java.time.LocalDate?,
    val status: NotificationStatus,
    val createdAt: LocalDateTime,
)

package com.metamong.application.notification.response

import com.metamong.application.notification.dto.NotificationDto
import com.metamong.domain.subscription.model.NotificationStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val complexId: Long?,
    val complexName: String?,
    val exclusivePyeong: Int?,
    val price: Long?,
    val contractDate: LocalDate?,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(dto: NotificationDto): NotificationResponse =
            NotificationResponse(
                id = dto.id,
                complexId = dto.complexId,
                complexName = dto.complexName,
                exclusivePyeong = dto.exclusivePyeong,
                price = dto.price,
                contractDate = dto.contractDate,
                isRead = dto.status == NotificationStatus.READ,
                createdAt = dto.createdAt,
            )
    }
}

data class UnreadCountResponse(
    val count: Long,
)

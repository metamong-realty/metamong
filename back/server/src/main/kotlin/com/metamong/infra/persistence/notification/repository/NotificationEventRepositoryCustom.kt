package com.metamong.infra.persistence.notification.repository

import com.metamong.application.notification.dto.NotificationDto
import org.springframework.data.domain.Pageable

interface NotificationEventRepositoryCustom {
    fun findNotificationsWithDetail(
        userId: Long,
        pageable: Pageable,
    ): List<NotificationDto>

    fun markAsRead(
        userId: Long,
        notificationId: Long,
    ): Long

    fun markAllAsRead(userId: Long): Long
}

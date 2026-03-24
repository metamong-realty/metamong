package com.metamong.application.notification.service

import com.metamong.infra.persistence.notification.repository.NotificationEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NotificationCommandService(
    private val notificationEventRepository: NotificationEventRepository,
) {
    fun markAsRead(
        userId: Long,
        notificationId: Long,
    ) {
        notificationEventRepository.markAsRead(userId, notificationId)
    }

    fun markAllAsRead(userId: Long) {
        notificationEventRepository.markAllAsRead(userId)
    }
}

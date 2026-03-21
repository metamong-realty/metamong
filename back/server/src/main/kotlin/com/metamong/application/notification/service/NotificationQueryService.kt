package com.metamong.application.notification.service

import com.metamong.application.notification.response.NotificationResponse
import com.metamong.domain.subscription.model.NotificationStatus
import com.metamong.infra.persistence.notification.repository.NotificationEventRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationQueryService(
    private val notificationEventRepository: NotificationEventRepository,
) {
    fun getNotifications(
        userId: Long,
        page: Int = 0,
        size: Int = 30,
    ): List<NotificationResponse> {
        val pageable = PageRequest.of(page, size)
        return notificationEventRepository
            .findNotificationsWithDetail(userId, pageable)
            .map { NotificationResponse.from(it) }
    }

    fun getUnreadCount(userId: Long): Long =
        notificationEventRepository.countByUserIdAndStatusIn(
            userId,
            listOf(NotificationStatus.PENDING),
        )
}

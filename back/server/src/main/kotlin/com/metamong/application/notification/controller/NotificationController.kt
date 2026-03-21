package com.metamong.application.notification.controller

import com.metamong.application.notification.response.NotificationResponse
import com.metamong.application.notification.response.UnreadCountResponse
import com.metamong.application.notification.service.NotificationCommandService
import com.metamong.application.notification.service.NotificationQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/notifications")
@Tag(name = "알림 API", description = "구독 알림 조회 및 읽음 처리 API")
class NotificationController(
    private val notificationQueryService: NotificationQueryService,
    private val notificationCommandService: NotificationCommandService,
) {
    @Operation(summary = "내 알림 목록 조회")
    @GetMapping
    fun getNotifications(
        @CurrentUser userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "30") size: Int,
    ): ApiResponse<List<NotificationResponse>> {
        val result = notificationQueryService.getNotifications(userId, page, size)
        return ApiResponse.ok(result)
    }

    @Operation(summary = "읽지 않은 알림 수 조회")
    @GetMapping("/unread-count")
    fun getUnreadCount(
        @CurrentUser userId: Long,
    ): ApiResponse<UnreadCountResponse> {
        val count = notificationQueryService.getUnreadCount(userId)
        return ApiResponse.ok(UnreadCountResponse(count))
    }

    @Operation(summary = "알림 읽음 처리")
    @PutMapping("/{id}/read")
    fun markAsRead(
        @CurrentUser userId: Long,
        @PathVariable id: Long,
    ): ApiResponse<Unit> {
        notificationCommandService.markAsRead(userId, id)
        return ApiResponse.ok(Unit)
    }

    @Operation(summary = "전체 알림 읽음 처리")
    @PutMapping("/read-all")
    fun markAllAsRead(
        @CurrentUser userId: Long,
    ): ApiResponse<Unit> {
        notificationCommandService.markAllAsRead(userId)
        return ApiResponse.ok(Unit)
    }
}

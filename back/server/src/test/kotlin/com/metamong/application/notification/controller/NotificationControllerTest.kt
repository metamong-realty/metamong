package com.metamong.application.notification.controller

import com.metamong.application.notification.response.NotificationResponse
import com.metamong.application.notification.service.NotificationCommandService
import com.metamong.application.notification.service.NotificationQueryService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [NotificationController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        SecurityFilterAutoConfiguration::class,
        OAuth2ClientAutoConfiguration::class,
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ["com\\.metamong\\.infra\\.security\\..*", "com\\.metamong\\.config\\.SecurityConfig"],
        ),
    ],
)
@DisplayName("NotificationController 테스트")
class NotificationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var notificationQueryService: NotificationQueryService

    @MockkBean
    private lateinit var notificationCommandService: NotificationCommandService

    // Security 제외 환경에서는 @CurrentUser가 null → userId=0L로 처리됨
    // 실제 인증 흐름은 JwtAuthenticationFilter 통합 테스트에서 검증

    private val sampleNotifications =
        listOf(
            NotificationResponse(
                id = 1L,
                complexId = 100L,
                complexName = "은마아파트",
                exclusivePyeong = 23,
                price = 375000L,
                contractDate = LocalDate.of(2026, 1, 7),
                isRead = false,
                createdAt = LocalDateTime.of(2026, 1, 8, 0, 0),
            ),
            NotificationResponse(
                id = 2L,
                complexId = 200L,
                complexName = "도곡렉슬",
                exclusivePyeong = 33,
                price = 500000L,
                contractDate = LocalDate.of(2025, 12, 20),
                isRead = true,
                createdAt = LocalDateTime.of(2025, 12, 21, 0, 0),
            ),
        )

    @Nested
    @DisplayName("GET /v1/notifications")
    inner class GetNotifications {
        @BeforeEach
        fun setup() {
            every { notificationQueryService.getNotifications(any(), 0, 30) } returns sampleNotifications
        }

        @Test
        @DisplayName("알림 목록을 조회한다")
        fun `알림 목록 조회 성공`() {
            mockMvc
                .perform(get("/v1/notifications"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].complexName").value("은마아파트"))
                .andExpect(jsonPath("$.data[0].exclusivePyeong").value(23))
                .andExpect(jsonPath("$.data[0].price").value(375000))
                .andExpect(jsonPath("$.data[0].isRead").value(false))
                .andExpect(jsonPath("$.data[1].isRead").value(true))
        }

        @Test
        @DisplayName("알림이 없을 때 빈 배열을 반환한다")
        fun `알림 없을 때 빈 배열 반환`() {
            every { notificationQueryService.getNotifications(any(), 0, 30) } returns emptyList()

            mockMvc
                .perform(get("/v1/notifications"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(0))
        }

        @Test
        @DisplayName("page, size 파라미터를 전달할 수 있다")
        fun `페이지 파라미터 전달`() {
            every { notificationQueryService.getNotifications(any(), 1, 10) } returns emptyList()

            mockMvc
                .perform(get("/v1/notifications?page=1&size=10"))
                .andExpect(status().isOk)

            verify { notificationQueryService.getNotifications(any(), 1, 10) }
        }
    }

    @Nested
    @DisplayName("GET /v1/notifications/unread-count")
    inner class GetUnreadCount {
        @Test
        @DisplayName("읽지 않은 알림 수를 반환한다")
        fun `읽지 않은 알림 수 조회`() {
            every { notificationQueryService.getUnreadCount(any()) } returns 5L

            mockMvc
                .perform(get("/v1/notifications/unread-count"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.count").value(5))
        }

        @Test
        @DisplayName("읽지 않은 알림이 없으면 0을 반환한다")
        fun `읽지 않은 알림 없을 때 0 반환`() {
            every { notificationQueryService.getUnreadCount(any()) } returns 0L

            mockMvc
                .perform(get("/v1/notifications/unread-count"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.count").value(0))
        }
    }

    @Nested
    @DisplayName("PUT /v1/notifications/{id}/read")
    inner class MarkAsRead {
        @Test
        @DisplayName("알림을 읽음 처리한다")
        fun `알림 읽음 처리 성공`() {
            every { notificationCommandService.markAsRead(any(), eq(1L)) } just runs

            mockMvc
                .perform(put("/v1/notifications/1/read"))
                .andExpect(status().isOk)

            verify { notificationCommandService.markAsRead(any(), eq(1L)) }
        }
    }

    @Nested
    @DisplayName("PUT /v1/notifications/read-all")
    inner class MarkAllAsRead {
        @Test
        @DisplayName("전체 알림을 읽음 처리한다")
        fun `전체 알림 읽음 처리 성공`() {
            every { notificationCommandService.markAllAsRead(any()) } just runs

            mockMvc
                .perform(put("/v1/notifications/read-all"))
                .andExpect(status().isOk)

            verify { notificationCommandService.markAllAsRead(any()) }
        }
    }
}

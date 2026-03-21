package com.metamong.application.notification.service

import com.metamong.application.notification.dto.NotificationDto
import com.metamong.domain.subscription.model.NotificationStatus
import com.metamong.infra.persistence.notification.repository.NotificationEventRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationQueryServiceTest :
    BehaviorSpec({
        val notificationEventRepository = mockk<NotificationEventRepository>()
        val notificationQueryService = NotificationQueryService(notificationEventRepository)

        Given("알림이 2개 있을 때") {
            val dtos =
                listOf(
                    NotificationDto(
                        id = 1L,
                        complexId = 100L,
                        complexName = "은마아파트",
                        exclusivePyeong = 23,
                        price = 375000L,
                        contractDate = LocalDate.of(2026, 1, 7),
                        status = NotificationStatus.PENDING,
                        createdAt = LocalDateTime.of(2026, 1, 8, 0, 0),
                    ),
                    NotificationDto(
                        id = 2L,
                        complexId = 200L,
                        complexName = "도곡렉슬",
                        exclusivePyeong = 33,
                        price = 500000L,
                        contractDate = LocalDate.of(2025, 12, 20),
                        status = NotificationStatus.READ,
                        createdAt = LocalDateTime.of(2025, 12, 21, 0, 0),
                    ),
                )

            every { notificationEventRepository.findNotificationsWithDetail(1L, any()) } returns dtos

            When("알림 목록을 조회하면") {
                val result = notificationQueryService.getNotifications(userId = 1L, page = 0, size = 30)

                Then("2개의 알림이 반환된다") {
                    result.size shouldBe 2
                }

                Then("PENDING 상태는 isRead=false로 변환된다") {
                    result[0].isRead shouldBe false
                    result[0].complexName shouldBe "은마아파트"
                    result[0].exclusivePyeong shouldBe 23
                    result[0].price shouldBe 375000L
                }

                Then("READ 상태는 isRead=true로 변환된다") {
                    result[1].isRead shouldBe true
                }

                Then("page=0, size=30으로 repository를 호출한다") {
                    verify { notificationEventRepository.findNotificationsWithDetail(1L, PageRequest.of(0, 30)) }
                }
            }

            When("page=1, size=10으로 조회하면") {
                every { notificationEventRepository.findNotificationsWithDetail(1L, PageRequest.of(1, 10)) } returns emptyList()

                val result = notificationQueryService.getNotifications(userId = 1L, page = 1, size = 10)

                Then("해당 페이지의 데이터를 반환한다") {
                    result.size shouldBe 0
                    verify { notificationEventRepository.findNotificationsWithDetail(1L, PageRequest.of(1, 10)) }
                }
            }
        }

        Given("읽지 않은 알림이 3개 있을 때") {
            every {
                notificationEventRepository.countByUserIdAndStatusIn(
                    1L,
                    listOf(NotificationStatus.PENDING),
                )
            } returns 3L

            When("읽지 않은 알림 수를 조회하면") {
                val count = notificationQueryService.getUnreadCount(userId = 1L)

                Then("3을 반환한다") {
                    count shouldBe 3L
                }
            }
        }

        Given("읽지 않은 알림이 없을 때") {
            every {
                notificationEventRepository.countByUserIdAndStatusIn(
                    1L,
                    listOf(NotificationStatus.PENDING),
                )
            } returns 0L

            When("읽지 않은 알림 수를 조회하면") {
                val count = notificationQueryService.getUnreadCount(userId = 1L)

                Then("0을 반환한다") {
                    count shouldBe 0L
                }
            }
        }
    })

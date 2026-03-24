package com.metamong.application.notification.service

import com.metamong.infra.persistence.notification.repository.NotificationEventRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotificationCommandServiceTest :
    BehaviorSpec({
        val notificationEventRepository = mockk<NotificationEventRepository>()
        val notificationCommandService = NotificationCommandService(notificationEventRepository)

        Given("알림이 존재할 때") {
            When("특정 알림을 읽음 처리하면") {
                every { notificationEventRepository.markAsRead(1L, 10L) } returns 1L

                notificationCommandService.markAsRead(userId = 1L, notificationId = 10L)

                Then("해당 알림의 읽음 처리를 호출한다") {
                    verify(exactly = 1) { notificationEventRepository.markAsRead(1L, 10L) }
                }
            }

            When("다른 유저의 알림을 읽음 처리하면") {
                every { notificationEventRepository.markAsRead(2L, 10L) } returns 0L

                notificationCommandService.markAsRead(userId = 2L, notificationId = 10L)

                Then("userId 조건으로 필터링되어 0건 처리된다") {
                    verify(exactly = 1) { notificationEventRepository.markAsRead(2L, 10L) }
                }
            }
        }

        Given("읽지 않은 알림이 여러 개 있을 때") {
            When("전체 읽음 처리하면") {
                every { notificationEventRepository.markAllAsRead(1L) } returns 5L

                notificationCommandService.markAllAsRead(userId = 1L)

                Then("해당 유저의 전체 읽음 처리를 호출한다") {
                    verify(exactly = 1) { notificationEventRepository.markAllAsRead(1L) }
                }
            }
        }

        Given("읽지 않은 알림이 없을 때") {
            When("전체 읽음 처리하면") {
                every { notificationEventRepository.markAllAsRead(1L) } returns 0L

                notificationCommandService.markAllAsRead(userId = 1L)

                Then("0건 처리된다") {
                    verify(exactly = 1) { notificationEventRepository.markAllAsRead(1L) }
                }
            }
        }
    })

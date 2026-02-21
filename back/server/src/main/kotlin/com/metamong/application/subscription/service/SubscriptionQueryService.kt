package com.metamong.application.subscription.service

import com.metamong.application.subscription.response.SubscriptionResponse
import com.metamong.domain.subscription.exception.SubscriptionException
import com.metamong.domain.subscription.model.SubscriptionEntity
import com.metamong.infra.persistence.subscription.repository.SubscriptionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SubscriptionQueryService(
    private val subscriptionRepository: SubscriptionRepository,
) {
    fun getMySubscriptions(userId: Long): List<SubscriptionResponse> =
        subscriptionRepository
            .findByUserIdOrderByCreatedAtDesc(userId)
            .map { SubscriptionResponse.from(it) }

    fun getSubscription(
        id: Long,
        userId: Long,
    ): SubscriptionResponse {
        val subscription = findById(id)
        validateOwnership(subscription, userId)
        return SubscriptionResponse.from(subscription)
    }

    private fun findById(id: Long): SubscriptionEntity =
        subscriptionRepository.findByIdOrNull(id)
            ?: throw SubscriptionException.NotFound()

    private fun validateOwnership(
        subscription: SubscriptionEntity,
        userId: Long,
    ) {
        if (subscription.userId != userId) {
            throw SubscriptionException.AccessDenied()
        }
    }
}

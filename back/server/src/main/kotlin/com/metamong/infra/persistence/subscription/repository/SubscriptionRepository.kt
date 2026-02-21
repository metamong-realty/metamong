package com.metamong.infra.persistence.subscription.repository

import com.metamong.domain.subscription.model.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionRepository : JpaRepository<SubscriptionEntity, Long> {
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<SubscriptionEntity>

    fun countByUserId(userId: Long): Long
}

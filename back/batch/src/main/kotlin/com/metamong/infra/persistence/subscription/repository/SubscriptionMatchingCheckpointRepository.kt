package com.metamong.infra.persistence.subscription.repository

import com.metamong.domain.subscription.model.SubscriptionMatchingCheckpointEntity
import com.metamong.domain.subscription.model.TradeType
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionMatchingCheckpointRepository : JpaRepository<SubscriptionMatchingCheckpointEntity, Long> {
    fun findByTradeType(tradeType: TradeType): SubscriptionMatchingCheckpointEntity?
}

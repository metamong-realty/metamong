package com.metamong.domain.subscription.model

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "subscription_matching_checkpoints")
class SubscriptionMatchingCheckpointEntity(
    @Enumerated(EnumType.STRING)
    val tradeType: TradeType,
    var lastProcessedTradeId: Long = 0,
    var processedAt: LocalDateTime = LocalDateTime.now(),
) : BaseEntity() {
    fun updateCheckpoint(lastTradeId: Long) {
        this.lastProcessedTradeId = lastTradeId
        this.processedAt = LocalDateTime.now()
    }
}

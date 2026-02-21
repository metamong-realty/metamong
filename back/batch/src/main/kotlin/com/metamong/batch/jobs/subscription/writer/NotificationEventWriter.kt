package com.metamong.batch.jobs.subscription.writer

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.domain.subscription.model.SubscriptionMatchingCheckpointEntity
import com.metamong.domain.subscription.model.TradeType
import com.metamong.infra.persistence.subscription.repository.SubscriptionMatchingCheckpointRepository
import com.metamong.service.subscription.SubscriptionMatchingService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.LocalDateTime

@Component
class NotificationEventWriter(
    private val subscriptionMatchingService: SubscriptionMatchingService,
    private val checkpointRepository: SubscriptionMatchingCheckpointRepository,
    private val jdbcTemplate: JdbcTemplate,
) : ItemWriter<ApartmentTradeEntity> {
    override fun write(chunk: Chunk<out ApartmentTradeEntity>) {
        val trades = chunk.items
        if (trades.isEmpty()) return

        val allEvents = subscriptionMatchingService.matchTrades(trades)

        if (allEvents.isNotEmpty()) {
            val now = Timestamp.valueOf(LocalDateTime.now())
            jdbcTemplate.batchUpdate(
                INSERT_SQL,
                allEvents,
                allEvents.size,
            ) { ps, event ->
                ps.setLong(1, event.userId)
                ps.setLong(2, event.subscriptionId)
                ps.setLong(3, event.tradeId)
                ps.setString(4, event.status.name)
                ps.setTimestamp(5, now)
                ps.setTimestamp(6, now)
            }
            logger.info { "알림 이벤트 ${allEvents.size}건 JDBC batch 저장 완료" }
        }

        val maxTradeId = trades.mapNotNull { it.id }.maxOrNull() ?: return
        updateCheckpoint(maxTradeId)
    }

    private fun updateCheckpoint(lastTradeId: Long) {
        val checkpoint = checkpointRepository.findByTradeType(TradeType.TRADE)
        if (checkpoint != null) {
            checkpoint.updateCheckpoint(lastTradeId)
            checkpointRepository.save(checkpoint)
        } else {
            checkpointRepository.save(
                SubscriptionMatchingCheckpointEntity(
                    tradeType = TradeType.TRADE,
                    lastProcessedTradeId = lastTradeId,
                ),
            )
        }
        logger.info { "체크포인트 갱신: lastProcessedTradeId=$lastTradeId" }
    }

    companion object {
        private const val INSERT_SQL =
            "INSERT INTO notification_events (user_id, subscription_id, trade_id, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
        private val logger = KotlinLogging.logger {}
    }
}

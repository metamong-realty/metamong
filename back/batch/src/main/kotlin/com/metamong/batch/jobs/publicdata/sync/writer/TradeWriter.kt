package com.metamong.batch.jobs.publicdata.sync.writer

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.service.apartment.ApartmentTradeSyncService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class TradeWriter(
    private val apartmentTradeSyncService: ApartmentTradeSyncService,
) : ItemWriter<ApartmentTradeEntity?> {
    override fun write(chunk: Chunk<out ApartmentTradeEntity?>) {
        val validItems = chunk.items.filterNotNull()
        if (validItems.isNotEmpty()) {
            val count = apartmentTradeSyncService.batchUpsertTrades(validItems)
            logger.info { "Trade 동기화 완료: ${count}건" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

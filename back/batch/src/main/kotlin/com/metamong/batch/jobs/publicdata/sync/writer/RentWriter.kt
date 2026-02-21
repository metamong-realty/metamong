package com.metamong.batch.jobs.publicdata.sync.writer

import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.service.apartment.ApartmentTradeSyncCommandService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class RentWriter(
    private val apartmentTradeSyncCommandService: ApartmentTradeSyncCommandService,
) : ItemWriter<ApartmentRentEntity?> {
    override fun write(chunk: Chunk<out ApartmentRentEntity?>) {
        val validItems = chunk.items.filterNotNull()
        if (validItems.isNotEmpty()) {
            val first = validItems.first()
            logger.info {
                "Rent 동기화 시작: contractYear=${first.contractYear}, contractMonth=${first.contractMonth}, " +
                    "rawId=${first.rawId}, 총 ${validItems.size}건"
            }
            val count = apartmentTradeSyncCommandService.batchUpsertRents(validItems)
            logger.info { "Rent 동기화 완료: ${count}건" }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

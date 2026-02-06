package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.entity.apartment.ApartmentTradeEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.ApartmentTradeSyncService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class SyncTradeProcessor(
    private val apartmentTradeSyncService: ApartmentTradeSyncService,
) : ItemProcessor<ApartmentTradeRawDocumentEntity, ApartmentTradeEntity?> {
    override fun process(item: ApartmentTradeRawDocumentEntity): ApartmentTradeEntity? = apartmentTradeSyncService.buildTradeFromRaw(item)
}

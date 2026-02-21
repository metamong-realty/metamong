package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.ApartmentTradeSyncQueryService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class SyncTradeProcessor(
    private val apartmentTradeSyncQueryService: ApartmentTradeSyncQueryService,
) : ItemProcessor<ApartmentTradeRawDocumentEntity, ApartmentTradeEntity?> {
    override fun process(item: ApartmentTradeRawDocumentEntity): ApartmentTradeEntity? =
        apartmentTradeSyncQueryService.buildTradeFromRaw(item)
}

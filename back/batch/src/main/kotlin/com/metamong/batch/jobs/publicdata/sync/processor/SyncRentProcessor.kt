package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.service.apartment.ApartmentTradeSyncQueryService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class SyncRentProcessor(
    private val apartmentTradeSyncQueryService: ApartmentTradeSyncQueryService,
) : ItemProcessor<ApartmentRentRawDocumentEntity, ApartmentRentEntity?> {
    override fun process(item: ApartmentRentRawDocumentEntity): ApartmentRentEntity? = apartmentTradeSyncQueryService.buildRentFromRaw(item)
}

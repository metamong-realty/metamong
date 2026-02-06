package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.entity.apartment.ApartmentRentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.service.apartment.ApartmentTradeSyncService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class SyncRentProcessor(
    private val apartmentTradeSyncService: ApartmentTradeSyncService,
) : ItemProcessor<ApartmentRentRawDocumentEntity, ApartmentRentEntity?> {
    override fun process(item: ApartmentRentRawDocumentEntity): ApartmentRentEntity? = apartmentTradeSyncService.buildRentFromRaw(item)
}

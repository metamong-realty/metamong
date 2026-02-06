package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.entity.apartment.ApartmentComplexEntity
import com.metamong.service.apartment.ApartmentMatchingService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class MatchInfoRawProcessor(
    private val apartmentMatchingService: ApartmentMatchingService,
) : ItemProcessor<ApartmentComplexEntity, Boolean> {
    override fun process(item: ApartmentComplexEntity): Boolean = apartmentMatchingService.matchInfoRaw(item)
}

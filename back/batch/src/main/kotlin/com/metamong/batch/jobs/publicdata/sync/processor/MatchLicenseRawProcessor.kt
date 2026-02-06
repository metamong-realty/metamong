package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.service.apartment.ApartmentMatchingService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class MatchLicenseRawProcessor(
    private val apartmentMatchingService: ApartmentMatchingService,
) : ItemProcessor<ApartmentComplexEntity, Boolean> {
    override fun process(item: ApartmentComplexEntity): Boolean = apartmentMatchingService.matchLicenseRaw(item)
}

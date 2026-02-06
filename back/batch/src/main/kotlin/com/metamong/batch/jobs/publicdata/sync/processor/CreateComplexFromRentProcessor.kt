package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.service.apartment.ApartmentComplexCommandService
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CreateComplexFromRentProcessor(
    private val apartmentComplexCommandService: ApartmentComplexCommandService,
) : ItemProcessor<ApartmentRentRawDocumentEntity, ComplexWithApartmentSequence?> {
    override fun process(item: ApartmentRentRawDocumentEntity): ComplexWithApartmentSequence? =
        apartmentComplexCommandService.buildComplexFromRentRaw(item)
}

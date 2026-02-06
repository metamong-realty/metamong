package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.ApartmentComplexCommandService
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CreateComplexProcessor(
    private val apartmentComplexCommandService: ApartmentComplexCommandService,
) : ItemProcessor<ApartmentTradeRawDocumentEntity, ComplexWithApartmentSequence?> {
    override fun process(item: ApartmentTradeRawDocumentEntity): ComplexWithApartmentSequence? =
        apartmentComplexCommandService.buildComplexFromTradeRaw(item)
}

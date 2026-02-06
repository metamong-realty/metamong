package com.metamong.batch.jobs.publicdata.sync.writer

import com.metamong.service.apartment.ApartmentComplexCommandService
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class ComplexWriter(
    private val apartmentComplexCommandService: ApartmentComplexCommandService,
) : ItemWriter<ComplexWithApartmentSequence?> {
    override fun write(chunk: org.springframework.batch.item.Chunk<out ComplexWithApartmentSequence?>) {
        val validItems = chunk.items.filterNotNull()
        if (validItems.isNotEmpty()) {
            apartmentComplexCommandService.saveAllComplexesWithMappings(validItems)
        }
    }
}

package com.metamong.batch.jobs.publicdata.sync.writer

import com.metamong.service.apartment.ApartmentComplexCommandService
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class ComplexWriter(
    private val apartmentComplexCommandService: ApartmentComplexCommandService,
) : ItemWriter<ComplexWithApartmentSequence?> {
    override fun write(chunk: Chunk<out ComplexWithApartmentSequence?>) {
        val validItems = chunk.items.filterNotNull()
        if (validItems.isNotEmpty()) {
            apartmentComplexCommandService.saveAllComplexesWithMappings(validItems)
        }
    }
}

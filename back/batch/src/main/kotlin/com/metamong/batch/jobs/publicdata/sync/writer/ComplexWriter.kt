package com.metamong.batch.jobs.publicdata.sync.writer

import com.metamong.service.apartment.ApartmentComplexCommandService
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import io.github.oshai.kotlinlogging.KotlinLogging
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
            val first = validItems.first()
            logger.info {
                "Complex 동기화 시작: sidoSigunguCode=${first.complex.sidoSigunguCode}, " +
                    "아파트명=${first.complex.nameRaw}, 총 ${validItems.size}건"
            }
            apartmentComplexCommandService.saveAllComplexesWithMappings(validItems)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

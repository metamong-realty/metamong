package com.metamong.batch.jobs.publicdata.sync.writer

import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity
import com.metamong.infra.persistence.apartment.repository.ApartmentCodeMappingRepository
import com.metamong.infra.persistence.apartment.repository.ApartmentComplexRepository
import com.metamong.service.apartment.dto.MatchResult
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class MatchResultWriter(
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val apartmentCodeMappingRepository: ApartmentCodeMappingRepository,
) : ItemWriter<MatchResult> {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun write(chunk: Chunk<out MatchResult>) {
        val items = chunk.items
        if (items.isEmpty()) return

        apartmentComplexRepository.batchUpdate(items.map { it.complex })

        val codeMappings =
            items.mapNotNull { result ->
                val type = result.codeMappingType ?: return@mapNotNull null
                val value = result.codeMappingValue ?: return@mapNotNull null
                ApartmentCodeMappingEntity.create(
                    complexId = result.complex.id,
                    codeType = type,
                    codeValue = value,
                )
            }
        if (codeMappings.isNotEmpty()) {
            apartmentCodeMappingRepository.batchInsertIgnore(codeMappings)
        }

        entityManager.clear()

        logger.info { "매칭 배치 저장 완료: ${items.size}건 (codeMappings: ${codeMappings.size}건)" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

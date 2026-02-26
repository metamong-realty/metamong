package com.metamong.batch.jobs.publicdata.sync.tasklet

import com.metamong.batch.jobs.publicdata.sync.cache.InMemoryMigrationCache
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.infra.persistence.apartment.repository.ApartmentUnitTypeJdbcRepository
import com.metamong.infra.persistence.mongo.publicdata.repository.ApartmentRentRawRepository
import com.metamong.infra.persistence.mongo.publicdata.repository.ApartmentTradeRawRepository
import com.metamong.util.apartment.AreaConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component

@Component
class CreateUnitTypeTasklet(
    private val tradeRawRepository: ApartmentTradeRawRepository,
    private val rentRawRepository: ApartmentRentRawRepository,
    private val inMemoryMigrationCache: InMemoryMigrationCache,
    private val apartmentUnitTypeJdbcRepository: ApartmentUnitTypeJdbcRepository,
) : Tasklet {
    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext,
    ): RepeatStatus {
        val distinctPairs = collectDistinctPairs()
        logger.info { "MongoDB에서 수집된 distinct (aptSeq, excluUseAr) 쌍: ${distinctPairs.size}건" }

        val newEntities = buildNewUnitTypeEntities(distinctPairs)
        logger.info { "신규 생성 대상 UnitType: ${newEntities.size}건" }

        if (newEntities.isNotEmpty()) {
            val saved = apartmentUnitTypeJdbcRepository.batchInsert(newEntities)
            addToCache(saved)
            logger.info { "UnitType 배치 생성 완료: ${saved.size}건" }
        }

        return RepeatStatus.FINISHED
    }

    private fun collectDistinctPairs(): Set<Pair<String, String>> {
        val tradePairs = tradeRawRepository.findDistinctAptSeqAndExcluUseAr()
        val rentPairs = rentRawRepository.findDistinctAptSeqAndExcluUseAr()
        return (tradePairs + rentPairs).toSet()
    }

    private fun buildNewUnitTypeEntities(distinctPairs: Set<Pair<String, String>>): List<ApartmentUnitTypeEntity> =
        distinctPairs
            .mapNotNull { (aptSeq, excluUseAr) ->
                val complexId = inMemoryMigrationCache.getComplexId(aptSeq) ?: return@mapNotNull null
                val exclusiveArea = AreaConverter.parseExclusiveArea(excluUseAr) ?: return@mapNotNull null
                val exclusivePyeong = AreaConverter.toPyeong(exclusiveArea) ?: return@mapNotNull null
                complexId to exclusivePyeong
            }.distinct()
            .filter { (complexId, exclusivePyeong) ->
                inMemoryMigrationCache.getUnitTypeId(complexId, exclusivePyeong) == null
            }.map { (complexId, exclusivePyeong) ->
                ApartmentUnitTypeEntity.create(complexId = complexId, exclusivePyeong = exclusivePyeong)
            }

    private fun addToCache(entities: List<ApartmentUnitTypeEntity>) {
        val newEntries =
            entities.associate { entity ->
                "${entity.complexId}:${entity.exclusivePyeong}" to entity.id
            }
        inMemoryMigrationCache.addUnitTypes(newEntries)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

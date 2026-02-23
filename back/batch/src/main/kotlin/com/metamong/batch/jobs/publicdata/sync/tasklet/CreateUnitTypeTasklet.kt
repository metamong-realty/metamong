package com.metamong.batch.jobs.publicdata.sync.tasklet

import com.metamong.common.cache.CacheType
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.infra.persistence.apartment.repository.ApartmentUnitTypeJdbcRepository
import com.metamong.infra.persistence.mongo.publicdata.repository.ApartmentRentRawRepository
import com.metamong.infra.persistence.mongo.publicdata.repository.ApartmentTradeRawRepository
import com.metamong.service.apartment.ApartmentComplexQueryService
import com.metamong.util.apartment.AreaConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Component
class CreateUnitTypeTasklet(
    private val tradeRawRepository: ApartmentTradeRawRepository,
    private val rentRawRepository: ApartmentRentRawRepository,
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
    private val apartmentUnitTypeJdbcRepository: ApartmentUnitTypeJdbcRepository,
    private val cacheManager: CacheManager,
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

    private fun buildNewUnitTypeEntities(distinctPairs: Set<Pair<String, String>>): List<ApartmentUnitTypeEntity> {
        val unitTypeCache = cacheManager.getCache(CacheType.UNIT_TYPE)

        return distinctPairs.mapNotNull { (aptSeq, excluUseAr) ->
            val complexId = apartmentComplexQueryService.getComplexIdByApartmentSequence(aptSeq)
            if (complexId == null) {
                logger.debug { "Complex 없음: aptSeq=$aptSeq" }
                return@mapNotNull null
            }

            val exclusiveArea = AreaConverter.parseExclusiveArea(excluUseAr)
            if (exclusiveArea == null) {
                logger.debug { "전용면적 파싱 실패: excluUseAr=$excluUseAr" }
                return@mapNotNull null
            }

            val cacheKey = "$complexId:$exclusiveArea"
            if (unitTypeCache?.get(cacheKey) != null) {
                return@mapNotNull null
            }

            val exclusivePyeong = AreaConverter.toPyeong(exclusiveArea)
            ApartmentUnitTypeEntity.create(
                complexId = complexId,
                exclusiveArea = exclusiveArea,
                exclusivePyeong = exclusivePyeong,
            )
        }
    }

    private fun addToCache(entities: List<ApartmentUnitTypeEntity>) {
        val cache = cacheManager.getCache(CacheType.UNIT_TYPE) ?: return

        entities.forEach { entity ->
            val key = "${entity.complexId}:${entity.exclusiveArea}"
            cache.put(key, entity)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

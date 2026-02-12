package com.metamong.batch.jobs.publicdata.sync.tasklet

import com.metamong.common.cache.CacheType
import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.infra.persistence.repository.apartment.ApartmentCodeMappingRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentUnitTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Component
class CacheWarmingTasklet(
    private val apartmentCodeMappingRepository: ApartmentCodeMappingRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
    private val cacheManager: CacheManager,
) : Tasklet {
    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext,
    ): RepeatStatus {
        warmAptSeqToComplexIdCache()
        warmUnitTypeCache()
        return RepeatStatus.FINISHED
    }

    private fun warmAptSeqToComplexIdCache() {
        val cache =
            cacheManager.getCache(CacheType.APARTMENT_SEQUENCE_TO_COMPLEX_ID)
                ?: run {
                    logger.warn { "캐시를 찾을 수 없음: ${CacheType.APARTMENT_SEQUENCE_TO_COMPLEX_ID}" }
                    return
                }

        val mappings = apartmentCodeMappingRepository.findAllByCodeType(ApartmentCodeType.APT_SEQ)
        mappings.forEach { mapping ->
            cache.put(mapping.codeValue, mapping.complexId)
        }

        logger.info { "aptSeq->complexId 캐시 워밍 완료: ${mappings.size}건" }
    }

    private fun warmUnitTypeCache() {
        val cache =
            cacheManager.getCache(CacheType.UNIT_TYPE)
                ?: run {
                    logger.warn { "캐시를 찾을 수 없음: ${CacheType.UNIT_TYPE}" }
                    return
                }

        val unitTypes = apartmentUnitTypeRepository.findAll()
        unitTypes.forEach { unitType ->
            val key = "${unitType.complexId}:${unitType.exclusiveArea}"
            cache.put(key, unitType)
        }

        logger.info { "unitType 캐시 워밍 완료: ${unitTypes.size}건" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

package com.metamong.service.apartment

import com.metamong.common.cache.CacheType
import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity
import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.infra.persistence.repository.apartment.ApartmentCodeMappingRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentComplexRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentUnitTypeRepository
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import com.metamong.util.apartment.AreaConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

@Service
@Transactional
class ApartmentComplexCommandService(
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val apartmentCodeMappingRepository: ApartmentCodeMappingRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
    private val cacheManager: CacheManager,
) {
    private val unitTypeLocalCache = ConcurrentHashMap<String, ApartmentUnitTypeEntity>()

    fun saveAllComplexesWithMappings(items: List<ComplexWithApartmentSequence>): List<ApartmentComplexEntity> {
        if (items.isEmpty()) return emptyList()

        val savedComplexes = apartmentComplexRepository.batchInsert(items.map { it.complex })

        val codeMappings =
            savedComplexes.zip(items).map { (saved, item) ->
                ApartmentCodeMappingEntity.create(
                    complexId = saved.id!!,
                    codeType = ApartmentCodeType.APT_SEQ,
                    codeValue = item.apartmentSequence,
                )
            }
        apartmentCodeMappingRepository.batchInsert(codeMappings)

        logger.info { "Complex 일괄 저장 완료: ${savedComplexes.size}건" }
        return savedComplexes
    }

    fun addCodeMapping(
        complexId: Long,
        codeType: ApartmentCodeType,
        codeValue: String,
    ): ApartmentCodeMappingEntity? {
        val existing = apartmentCodeMappingRepository.findByCodeTypeAndCodeValue(codeType, codeValue)
        if (existing != null) {
            if (existing.complexId != complexId) {
                logger.warn { "CodeMapping 이미 존재 (다른 Complex): $codeType=$codeValue, 기존=${existing.complexId}, 요청=$complexId" }
            }
            return null
        }

        val mapping =
            ApartmentCodeMappingEntity.create(
                complexId = complexId,
                codeType = codeType,
                codeValue = codeValue,
            )
        return apartmentCodeMappingRepository.save(mapping)
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    fun createOrGetUnitType(
//        complexId: Long,
//        exclusiveArea: BigDecimal,
//    ): ApartmentUnitTypeEntity {
//        val cacheKey = "$complexId:$exclusiveArea"
//
//        // L1: JVM 메모리 캐시
//        unitTypeLocalCache[cacheKey]?.let { return it }
//
//        // L2: Redis 캐시 (@Cacheable)
//        val existing = apartmentComplexQueryService.getUnitType(complexId, exclusiveArea)
//        if (existing != null) {
//            unitTypeLocalCache[cacheKey] = existing
//            return existing
//        }
//
//        // DB 직접 조회
//        val dbExisting = apartmentUnitTypeRepository.findByComplexIdAndExclusiveArea(complexId, exclusiveArea)
//        if (dbExisting != null) {
//            unitTypeLocalCache[cacheKey] = dbExisting
//            putUnitTypeCache(cacheKey, dbExisting)
//            return dbExisting
//        }
//
//        // 신규 생성
//        val exclusivePyeong = AreaConverter.toPyeong(exclusiveArea)
//        val unitType =
//            ApartmentUnitTypeEntity.create(
//                complexId = complexId,
//                exclusiveArea = exclusiveArea,
//                exclusivePyeong = exclusivePyeong,
//            )
//
//        return try {
//            val saved = apartmentUnitTypeRepository.saveAndFlush(unitType)
//            logger.debug { "UnitType 생성: complexId=$complexId, area=$exclusiveArea, pyeong=$exclusivePyeong" }
//            unitTypeLocalCache[cacheKey] = saved
//            putUnitTypeCache(cacheKey, saved)
//            saved
//        } catch (e: DataIntegrityViolationException) {
//            logger.debug { "UnitType 중복, 재조회: complexId=$complexId, area=$exclusiveArea" }
//            val found =
//                apartmentUnitTypeRepository.findByComplexIdAndExclusiveArea(complexId, exclusiveArea)
//                    ?: throw e
//            unitTypeLocalCache[cacheKey] = found
//            putUnitTypeCache(cacheKey, found)
//            found
//        }
//    }

    private fun putUnitTypeCache(
        key: String,
        entity: ApartmentUnitTypeEntity,
    ) {
        cacheManager.getCache(CacheType.UNIT_TYPE)?.put(key, entity)
    }

    fun saveComplex(complex: ApartmentComplexEntity): ApartmentComplexEntity = apartmentComplexRepository.save(complex)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

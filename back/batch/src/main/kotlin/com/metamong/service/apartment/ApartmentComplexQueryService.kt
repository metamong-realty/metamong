package com.metamong.service.apartment

import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.domain.apartment.model.PlatType
import com.metamong.infra.persistance.repository.apartment.ApartmentCodeMappingRepository
import com.metamong.infra.persistance.repository.apartment.ApartmentComplexRepository
import com.metamong.infra.persistance.repository.apartment.ApartmentUnitTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

@Service
@Transactional(readOnly = true)
class ApartmentComplexQueryService(
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val apartmentCodeMappingRepository: ApartmentCodeMappingRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
) {
    private val apartmentSequenceToComplexIdCache = ConcurrentHashMap<String, Long>()
    private val unitTypeCache = ConcurrentHashMap<String, ApartmentUnitTypeEntity>()

    fun getUnmatchedInfoRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity> = apartmentComplexRepository.findUnmatchedInfoRawComplexes(limit, offset)

    fun getUnmatchedLicenseRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity> = apartmentComplexRepository.findUnmatchedLicenseRawComplexes(limit, offset)

    fun getComplexIdByApartmentSequence(apartmentSequence: String): Long? {
        apartmentSequenceToComplexIdCache[apartmentSequence]?.let { return it }

        val complexId =
            apartmentCodeMappingRepository
                .findByCodeTypeAndCodeValue(ApartmentCodeType.APT_SEQ, apartmentSequence)
                ?.complexId

        if (complexId != null) {
            apartmentSequenceToComplexIdCache[apartmentSequence] = complexId
        }
        return complexId
    }

    fun existsByApartmentSequence(apartmentSequence: String): Boolean =
        apartmentCodeMappingRepository.existsByCodeTypeAndCodeValue(
            ApartmentCodeType.APT_SEQ,
            apartmentSequence,
        )

    fun getUnitType(
        complexId: Long,
        exclusiveArea: BigDecimal,
    ): ApartmentUnitTypeEntity? {
        val cacheKey = "$complexId:$exclusiveArea"
        unitTypeCache[cacheKey]?.let { return it }

        val unitType = apartmentUnitTypeRepository.findByComplexIdAndExclusiveArea(complexId, exclusiveArea)
        if (unitType != null) {
            unitTypeCache[cacheKey] = unitType
        }
        return unitType
    }

    fun cacheUnitType(unitType: ApartmentUnitTypeEntity) {
        val cacheKey = "${unitType.complexId}:${unitType.exclusiveArea}"
        unitTypeCache[cacheKey] = unitType
    }

    fun getUnitTypesByComplexId(complexId: Long): List<ApartmentUnitTypeEntity> = apartmentUnitTypeRepository.findAllByComplexId(complexId)

    fun findComplexBySidoSigunguAndNameAndBuiltYear(
        sidoSigunguCode: Int,
        nameNormalized: String,
        builtYear: Short,
    ): ApartmentComplexEntity? {
        val builtYearFrom = (builtYear - 1).toShort()
        val builtYearTo = (builtYear + 1).toShort()

        return apartmentComplexRepository.findBySidoSigunguAndNameNormalizedAndBuiltYearBetween(
            sidoSigunguCode,
            nameNormalized,
            builtYearFrom,
            builtYearTo,
        )
    }

    fun findComplexByJibun(
        sidoSigunguCode: Int,
        eupmyeondongRiCode: Int,
        platType: PlatType,
        bonNo: Short,
        buNo: Short,
    ): ApartmentComplexEntity? = apartmentComplexRepository.findByJibun(sidoSigunguCode, eupmyeondongRiCode, platType, bonNo, buNo)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

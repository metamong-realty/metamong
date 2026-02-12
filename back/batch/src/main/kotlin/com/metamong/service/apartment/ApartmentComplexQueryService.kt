package com.metamong.service.apartment

import com.metamong.common.cache.CacheType
import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.domain.apartment.model.PlatType
import com.metamong.infra.persistence.repository.apartment.ApartmentCodeMappingRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentComplexRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentUnitTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class ApartmentComplexQueryService(
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val apartmentCodeMappingRepository: ApartmentCodeMappingRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
) {
    fun getUnmatchedInfoRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity> = apartmentComplexRepository.findUnmatchedInfoRawComplexes(limit, offset)

    fun getUnmatchedLicenseRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity> = apartmentComplexRepository.findUnmatchedLicenseRawComplexes(limit, offset)

    @Cacheable(
        value = [CacheType.APARTMENT_SEQUENCE_TO_COMPLEX_ID],
        key = "#apartmentSequence",
        unless = "#result == null",
    )
    fun getComplexIdByApartmentSequence(apartmentSequence: String): Long? {
        val result =
            apartmentCodeMappingRepository
                .findByCodeTypeAndCodeValue(ApartmentCodeType.APT_SEQ, apartmentSequence)
                ?.complexId

        logger.debug { "getComplexIdByApartmentSequence - apartmentSequence: $apartmentSequence, result: $result" }
        return result
    }

    fun existsByApartmentSequence(apartmentSequence: String): Boolean =
        apartmentCodeMappingRepository.existsByCodeTypeAndCodeValue(
            ApartmentCodeType.APT_SEQ,
            apartmentSequence,
        )

    fun findExistingApartmentSequences(apartmentSequences: Collection<String>): Set<String> {
        if (apartmentSequences.isEmpty()) return emptySet()
        return apartmentCodeMappingRepository
            .findAllByCodeTypeAndCodeValueIn(ApartmentCodeType.APT_SEQ, apartmentSequences)
            .map { it.codeValue }
            .toSet()
    }

    @Cacheable(
        value = [CacheType.UNIT_TYPE],
        key = "#complexId + ':' + #exclusiveArea",
        unless = "#result == null",
    )
    fun getUnitType(
        complexId: Long,
        exclusiveArea: BigDecimal,
    ): ApartmentUnitTypeEntity? = apartmentUnitTypeRepository.findByComplexIdAndExclusiveArea(complexId, exclusiveArea)

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

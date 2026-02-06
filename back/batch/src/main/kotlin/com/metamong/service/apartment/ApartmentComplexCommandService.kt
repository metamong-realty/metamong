package com.metamong.service.apartment

import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity
import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.infra.persistence.repository.apartment.ApartmentCodeMappingRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentComplexRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentUnitTypeRepository
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import com.metamong.util.apartment.AddressParser
import com.metamong.util.apartment.ApartmentNameNormalizer
import com.metamong.util.apartment.AreaConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class ApartmentComplexCommandService(
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val apartmentCodeMappingRepository: ApartmentCodeMappingRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
) {
    fun buildComplexFromTradeRaw(tradeRaw: ApartmentTradeRawDocumentEntity): ComplexWithApartmentSequence? {
        val apartmentSequence = tradeRaw.aptSeq ?: return null
        val apartmentName = tradeRaw.aptNm ?: return null
        val sidoSigunguCode = tradeRaw.lawdCd.toIntOrNull() ?: return null

        val jibunResult = AddressParser.parseJibun(tradeRaw.jibun)
        val roadAddress =
            AddressParser.buildRoadAddress(
                tradeRaw.roadNm,
                tradeRaw.roadNmBonbun,
                tradeRaw.roadNmBubun,
            )
        val jibunAddress = AddressParser.buildJibunAddress(tradeRaw.umdNm, tradeRaw.jibun)
        val builtYear = tradeRaw.buildYear?.toShortOrNull()

        val complex =
            ApartmentComplexEntity.create(
                sidoSigunguCode = sidoSigunguCode,
                nameRaw = apartmentName,
                nameNormalized = ApartmentNameNormalizer.normalize(apartmentName),
                builtYear = builtYear,
                bonNo = jibunResult?.bonNo,
                buNo = jibunResult?.buNo,
                addressRoad = roadAddress,
                addressJibun = jibunAddress,
            )

        return ComplexWithApartmentSequence(complex, apartmentSequence)
    }

    fun buildComplexFromRentRaw(rentRaw: ApartmentRentRawDocumentEntity): ComplexWithApartmentSequence? {
        val apartmentSequence = rentRaw.aptSeq ?: return null
        val apartmentName = rentRaw.aptNm ?: return null
        val sidoSigunguCode = rentRaw.lawdCd.toIntOrNull() ?: return null

        val jibunResult = AddressParser.parseJibun(rentRaw.jibun)
        val jibunAddress = AddressParser.buildJibunAddress(rentRaw.umdNm, rentRaw.jibun)
        val builtYear = rentRaw.buildYear?.toShortOrNull()

        val complex =
            ApartmentComplexEntity.create(
                sidoSigunguCode = sidoSigunguCode,
                nameRaw = apartmentName,
                nameNormalized = ApartmentNameNormalizer.normalize(apartmentName),
                builtYear = builtYear,
                bonNo = jibunResult?.bonNo,
                buNo = jibunResult?.buNo,
                addressRoad = rentRaw.roadnm,
                addressJibun = jibunAddress,
            )

        return ComplexWithApartmentSequence(complex, apartmentSequence)
    }

    fun saveAllComplexesWithMappings(items: List<ComplexWithApartmentSequence>): List<ApartmentComplexEntity> {
        if (items.isEmpty()) return emptyList()

        val savedComplexes = apartmentComplexRepository.saveAll(items.map { it.complex })

        val codeMappings =
            savedComplexes.zip(items).map { (saved, item) ->
                ApartmentCodeMappingEntity.create(
                    complexId = saved.id!!,
                    codeType = ApartmentCodeType.APT_SEQ,
                    codeValue = item.apartmentSequence,
                )
            }
        apartmentCodeMappingRepository.saveAll(codeMappings)

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

    fun createOrGetUnitType(
        complexId: Long,
        exclusiveArea: BigDecimal,
    ): ApartmentUnitTypeEntity {
        val existing = apartmentComplexQueryService.getUnitType(complexId, exclusiveArea)
        if (existing != null) {
            return existing
        }

        val dbExisting = apartmentUnitTypeRepository.findByComplexIdAndExclusiveArea(complexId, exclusiveArea)
        if (dbExisting != null) {
            apartmentComplexQueryService.cacheUnitType(dbExisting)
            return dbExisting
        }

        val exclusivePyeong = AreaConverter.toPyeong(exclusiveArea)
        val unitType =
            ApartmentUnitTypeEntity.create(
                complexId = complexId,
                exclusiveArea = exclusiveArea,
                exclusivePyeong = exclusivePyeong,
            )

        return try {
            val saved = apartmentUnitTypeRepository.saveAndFlush(unitType)
            apartmentComplexQueryService.cacheUnitType(saved)
            logger.debug { "UnitType 생성: complexId=$complexId, area=$exclusiveArea, pyeong=$exclusivePyeong" }
            saved
        } catch (e: DataIntegrityViolationException) {
            logger.debug { "UnitType 중복, 재조회: complexId=$complexId, area=$exclusiveArea" }
            apartmentUnitTypeRepository.findByComplexIdAndExclusiveArea(complexId, exclusiveArea)
                ?: throw e
        }
    }

    fun saveComplex(complex: ApartmentComplexEntity): ApartmentComplexEntity = apartmentComplexRepository.save(complex)

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

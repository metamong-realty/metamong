package com.metamong.service.apartment

import com.metamong.domain.apartment.model.ApartmentCodeType
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.PlatType
import com.metamong.infra.persistence.repository.mongo.publicdata.ApartmentComplexInfoRawRepository
import com.metamong.infra.persistence.repository.mongo.publicdata.HousingLicenseRawRepository
import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import com.metamong.util.apartment.AddressParser
import com.metamong.util.apartment.ApartmentNameNormalizer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@Service
@Transactional
class ApartmentMatchingService(
    private val apartmentComplexInfoRawRepository: ApartmentComplexInfoRawRepository,
    private val housingLicenseRawRepository: HousingLicenseRawRepository,
    private val apartmentComplexCommandService: ApartmentComplexCommandService,
) {
    private val infoRawCacheBySidoSigungu = ConcurrentHashMap<Int, List<ApartmentComplexInfoRawDocumentEntity>>()
    private val licenseRawCacheBySidoSigunguEupmyeondongRi = ConcurrentHashMap<String, List<HousingLicenseRawDocumentEntity>>()

    fun matchInfoRaw(complex: ApartmentComplexEntity): Boolean {
        val nameNormalized = complex.nameNormalized ?: return false
        val builtYear = complex.builtYear ?: return false
        val sidoSigunguCode = complex.sidoSigunguCode
        val addressRoad = complex.addressRoad

        val infoRaw = findMatchingInfoRaw(sidoSigunguCode, nameNormalized, builtYear, addressRoad)
        if (infoRaw == null) {
            logger.debug { "InfoRaw 매칭 실패: complexId=${complex.id}, name=${complex.nameRaw}" }
            return false
        }

        val eupmyeondongRiCode = AddressParser.extractEupmyeondongRiCodeFromBjdCode(infoRaw.bjdCode)
        val jibunResult = AddressParser.parseKaptAddrJibun(infoRaw.kaptAddr)

        complex.updateFromInfoRaw(
            eupmyeondongRiCode = eupmyeondongRiCode,
            addressRoad = infoRaw.doroJuso,
            addressJibun = infoRaw.kaptAddr,
            bonNo = jibunResult?.bonNo,
            buNo = jibunResult?.buNo,
            totalHousehold = infoRaw.hoCnt,
            totalBuilding = infoRaw.kaptDongCnt?.toIntOrNull(),
            heatingType = infoRaw.codeHeatNm,
        )

        apartmentComplexCommandService.saveComplex(complex)

        apartmentComplexCommandService.addCodeMapping(
            complexId = complex.id!!,
            codeType = ApartmentCodeType.KAPT_CODE,
            codeValue = infoRaw.kaptCode,
        )

        logger.info { "InfoRaw 매칭 성공: complexId=${complex.id}, kaptCode=${infoRaw.kaptCode}" }
        return true
    }

    fun matchLicenseRaw(complex: ApartmentComplexEntity): Boolean {
        val sidoSigunguCode = complex.sidoSigunguCode
        val eupmyeondongRiCode = complex.eupmyeondongRiCode ?: return false
        val platType = complex.platType ?: PlatType.LAND
        val bonNo = complex.bonNo ?: return false
        val buNo = complex.buNo ?: return false

        val licenseRaw = findMatchingLicenseRaw(sidoSigunguCode, eupmyeondongRiCode, platType, bonNo, buNo)
        if (licenseRaw == null) {
            logger.debug { "LicenseRaw 매칭 실패: complexId=${complex.id}" }
            return false
        }

        val floorAreaRatio = calculateFloorAreaRatio(licenseRaw)

        complex.updateFromLicenseRaw(
            floorAreaRatio = floorAreaRatio,
            buildingCoverageRatio = null,
        )

        apartmentComplexCommandService.saveComplex(complex)

        licenseRaw.mgmHsrgstPk?.let { pk ->
            apartmentComplexCommandService.addCodeMapping(
                complexId = complex.id!!,
                codeType = ApartmentCodeType.LICENSE_PK,
                codeValue = pk,
            )
        }

        logger.info { "LicenseRaw 매칭 성공: complexId=${complex.id}, floorAreaRatio=$floorAreaRatio" }
        return true
    }

    /**
     * InfoRaw 매칭 전략 (우선순위 순):
     * 1. 정확한 이름 매칭: 정규화된 이름이 완전 일치 + 건축년도 ±1년
     * 2. 부분 이름 매칭: InfoRaw 이름이 Complex 이름을 포함 + 건축년도 ±1년 (단일 후보만)
     * 3. 도로명 주소 매칭: 도로명 주소 suffix 일치 + 건축년도 ±1년 (단일 후보만)
     *
     * 건축년도 ±1년 허용 이유: 임시 사용승인일 vs 준공일 차이로 오차 발생 가능
     */
    private fun findMatchingInfoRaw(
        sidoSigunguCode: Int,
        nameNormalized: String,
        builtYear: Short,
        addressRoad: String?,
    ): ApartmentComplexInfoRawDocumentEntity? {
        val candidates = getInfoRawCandidates(sidoSigunguCode)
        if (candidates.isEmpty()) return null

        return findByExactName(candidates, nameNormalized, builtYear)
            ?: findByPartialName(candidates, nameNormalized, builtYear)
            ?: findByRoadAddress(candidates, builtYear, addressRoad)
    }

    private fun getInfoRawCandidates(sidoSigunguCode: Int): List<ApartmentComplexInfoRawDocumentEntity> {
        val sidoSigunguCodeStr = sidoSigunguCode.toString().padStart(5, '0')
        return infoRawCacheBySidoSigungu.getOrPut(sidoSigunguCode) {
            logger.debug { "InfoRaw 캐시 로드: sidoSigunguCode=$sidoSigunguCode" }
            apartmentComplexInfoRawRepository.findByBjdCodeStartingWith("^$sidoSigunguCodeStr")
        }
    }

    private fun findByExactName(
        candidates: List<ApartmentComplexInfoRawDocumentEntity>,
        nameNormalized: String,
        builtYear: Short,
    ): ApartmentComplexInfoRawDocumentEntity? =
        candidates.firstOrNull { infoRaw ->
            val infoNameNormalized = ApartmentNameNormalizer.normalize(infoRaw.kaptName)
            infoNameNormalized == nameNormalized && isBuiltYearMatch(infoRaw, builtYear)
        }

    private fun findByPartialName(
        candidates: List<ApartmentComplexInfoRawDocumentEntity>,
        nameNormalized: String,
        builtYear: Short,
    ): ApartmentComplexInfoRawDocumentEntity? {
        val matches =
            candidates.filter { infoRaw ->
                val infoNameNormalized = ApartmentNameNormalizer.normalize(infoRaw.kaptName)
                isBuiltYearMatch(infoRaw, builtYear) &&
                    infoNameNormalized?.contains(nameNormalized) == true
            }
        if (matches.size > 1) {
            logger.debug { "부분 이름 매칭 다중 후보: name=$nameNormalized, count=${matches.size}" }
        }
        return matches.singleOrNull()
    }

    private fun findByRoadAddress(
        candidates: List<ApartmentComplexInfoRawDocumentEntity>,
        builtYear: Short,
        addressRoad: String?,
    ): ApartmentComplexInfoRawDocumentEntity? {
        if (addressRoad.isNullOrBlank()) return null

        val normalizedAddressRoad = normalizeRoadAddress(addressRoad)
        val matches =
            candidates.filter { infoRaw ->
                val doroJuso = infoRaw.doroJuso ?: return@filter false
                isBuiltYearMatch(infoRaw, builtYear) &&
                    normalizeRoadAddress(doroJuso).endsWith(normalizedAddressRoad)
            }
        if (matches.size > 1) {
            logger.debug { "도로명 주소 매칭 다중 후보: address=$addressRoad, count=${matches.size}" }
        }
        return matches.singleOrNull()
    }

    private fun isBuiltYearMatch(
        infoRaw: ApartmentComplexInfoRawDocumentEntity,
        builtYear: Short,
    ): Boolean {
        val infoBuiltYear = parseBuiltYear(infoRaw.kaptUsedate) ?: return false
        return abs(infoBuiltYear - builtYear) <= 1
    }

    private fun normalizeRoadAddress(address: String): String = address.replace(" ", "")

    private fun findMatchingLicenseRaw(
        sidoSigunguCode: Int,
        eupmyeondongRiCode: Int,
        platType: PlatType,
        bonNo: Short,
        buNo: Short,
    ): HousingLicenseRawDocumentEntity? {
        val cacheKey = "$sidoSigunguCode:$eupmyeondongRiCode"
        val sidoSigunguCodeStr = sidoSigunguCode.toString().padStart(5, '0')
        val eupmyeondongRiCodeStr = eupmyeondongRiCode.toString().padStart(5, '0')

        val allCandidates =
            licenseRawCacheBySidoSigunguEupmyeondongRi.getOrPut(cacheKey) {
                logger.debug { "LicenseRaw 캐시 로드: sidoSigunguCode=$sidoSigunguCode, eupmyeondongRiCode=$eupmyeondongRiCode" }
                housingLicenseRawRepository.findBySigunguCdAndBjdongCd(sidoSigunguCodeStr, eupmyeondongRiCodeStr)
            }

        val candidates =
            allCandidates.filter { raw ->
                PlatType.fromCode(raw.platGbCd) == platType &&
                    raw.bun?.toShortOrNull() == bonNo &&
                    raw.ji?.toShortOrNull() == buNo &&
                    raw.mainPurpsCdNm == "공동주택"
            }

        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        return candidates.maxByOrNull { raw ->
            raw.totArea?.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }
    }

    private fun parseBuiltYear(kaptUsedate: String?): Short? {
        if (kaptUsedate.isNullOrBlank() || kaptUsedate.length < 4) return null
        return kaptUsedate.substring(0, 4).toShortOrNull()
    }

    private fun calculateFloorAreaRatio(licenseRaw: HousingLicenseRawDocumentEntity): BigDecimal? {
        val vlRatEstmTotArea = licenseRaw.vlRatEstmTotArea?.toBigDecimalOrNull() ?: return null
        val archArea = licenseRaw.archArea?.toBigDecimalOrNull() ?: return null

        if (archArea < BigDecimal.ONE) return null
        if (vlRatEstmTotArea <= BigDecimal.ZERO) return null

        val ratio =
            vlRatEstmTotArea
                .divide(archArea, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))

        if (ratio > MAX_FLOOR_AREA_RATIO) {
            logger.warn { "용적률 이상값 제외: $ratio% (vlRat=$vlRatEstmTotArea, arch=$archArea)" }
            return null
        }

        return ratio
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        /**
         * 용적률 최대값 (2000%)
         * - 일반적인 아파트 용적률: 200~300%
         * - 초고층/주상복합: 800~1000%
         * - 2000% 초과 시 데이터 오류로 판단하여 제외
         */
        private val MAX_FLOOR_AREA_RATIO = BigDecimal("2000")
    }
}

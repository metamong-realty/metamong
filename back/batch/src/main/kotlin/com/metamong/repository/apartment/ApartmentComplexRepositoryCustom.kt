package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentComplexEntity
import com.metamong.enums.apartment.PlatType

interface ApartmentComplexRepositoryCustom {
    fun findUnmatchedInfoRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity>

    fun findUnmatchedLicenseRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity>

    fun findBySidoSigunguAndNameNormalizedAndBuiltYearBetween(
        sidoSigunguCode: Int,
        nameNormalized: String,
        builtYearFrom: Short,
        builtYearTo: Short,
    ): ApartmentComplexEntity?

    fun findByJibun(
        sidoSigunguCode: Int,
        eupmyeondongRiCode: Int,
        platType: PlatType,
        bonNo: Short,
        buNo: Short,
    ): ApartmentComplexEntity?
}

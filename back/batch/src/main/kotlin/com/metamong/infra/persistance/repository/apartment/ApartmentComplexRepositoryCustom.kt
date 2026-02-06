package com.metamong.infra.persistance.repository.apartment

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.PlatType

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

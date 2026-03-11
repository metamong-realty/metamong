package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.PlatType

interface ApartmentComplexRepositoryCustom {
    fun findUnmatchedInfoRawComplexes(
        limit: Long,
        lastId: Long,
    ): List<ApartmentComplexEntity>

    fun findUnmatchedLicenseRawComplexes(
        limit: Long,
        lastId: Long,
    ): List<ApartmentComplexEntity>

    fun findBySidoSigunguAndNameNormalizedAndBuiltYearBetween(
        sidoSigunguCode: Int,
        nameNormalized: String,
        builtYearFrom: Int,
        builtYearTo: Int,
    ): ApartmentComplexEntity?

    fun findByJibun(
        sidoSigunguCode: Int,
        eupmyeondongRiCode: Int,
        platType: PlatType,
        bonNo: Int,
        buNo: Int,
    ): ApartmentComplexEntity?
}

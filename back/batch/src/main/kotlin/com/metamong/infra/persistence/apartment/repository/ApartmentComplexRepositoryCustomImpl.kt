package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.PlatType
import com.metamong.domain.apartment.model.QApartmentComplexEntity
import com.metamong.support.QuerydslRepositorySupport

class ApartmentComplexRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentComplexEntity::class.java),
    ApartmentComplexRepositoryCustom {
    val complex: QApartmentComplexEntity = QApartmentComplexEntity.apartmentComplexEntity

    override fun findUnmatchedInfoRawComplexes(
        limit: Long,
        lastId: Long,
    ): List<ApartmentComplexEntity> =
        from(complex)
            .select(complex)
            .where(
                complex.id.gt(lastId),
                complex.eupmyeondongRiCode.isNull,
            ).orderBy(complex.id.asc())
            .limit(limit)
            .fetch()

    override fun findUnmatchedLicenseRawComplexes(
        limit: Long,
        lastId: Long,
    ): List<ApartmentComplexEntity> =
        from(complex)
            .select(complex)
            .where(
                complex.id.gt(lastId),
                complex.eupmyeondongRiCode.isNotNull,
                complex.floorAreaRatio.isNull,
            ).orderBy(complex.id.asc())
            .limit(limit)
            .fetch()

    override fun findBySidoSigunguAndNameNormalizedAndBuiltYearBetween(
        sidoSigunguCode: Int,
        nameNormalized: String,
        builtYearFrom: Int,
        builtYearTo: Int,
    ): ApartmentComplexEntity? =
        from(complex)
            .select(complex)
            .where(
                complex.sidoSigunguCode.eq(sidoSigunguCode),
                complex.nameNormalized.eq(nameNormalized),
                complex.builtYear.between(builtYearFrom, builtYearTo),
            ).fetchFirst()

    override fun findByJibun(
        sidoSigunguCode: Int,
        eupmyeondongRiCode: Int,
        platType: PlatType,
        bonNo: Int,
        buNo: Int,
    ): ApartmentComplexEntity? =
        from(complex)
            .select(complex)
            .where(
                complex.sidoSigunguCode.eq(sidoSigunguCode),
                complex.eupmyeondongRiCode.eq(eupmyeondongRiCode),
                complex.platType.eq(platType),
                complex.bonNo.eq(bonNo),
                complex.buNo.eq(buNo),
            ).fetchFirst()
}

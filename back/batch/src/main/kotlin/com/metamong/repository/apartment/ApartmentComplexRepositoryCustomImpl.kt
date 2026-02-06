package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentComplexEntity
import com.metamong.entity.apartment.QApartmentComplexEntity
import com.metamong.enums.apartment.PlatType
import com.metamong.support.QuerydslRepositorySupport

class ApartmentComplexRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentComplexEntity::class.java),
    ApartmentComplexRepositoryCustom {
    val complex: QApartmentComplexEntity = QApartmentComplexEntity.apartmentComplexEntity

    override fun findUnmatchedInfoRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity> =
        from(complex)
            .select(complex)
            .where(complex.eupmyeondongRiCode.isNull)
            .offset(offset)
            .limit(limit)
            .fetch()

    override fun findUnmatchedLicenseRawComplexes(
        limit: Long,
        offset: Long,
    ): List<ApartmentComplexEntity> =
        from(complex)
            .select(complex)
            .where(
                complex.eupmyeondongRiCode.isNotNull,
                complex.floorAreaRatio.isNull,
            ).offset(offset)
            .limit(limit)
            .fetch()

    override fun findBySidoSigunguAndNameNormalizedAndBuiltYearBetween(
        sidoSigunguCode: Int,
        nameNormalized: String,
        builtYearFrom: Short,
        builtYearTo: Short,
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
        bonNo: Short,
        buNo: Short,
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

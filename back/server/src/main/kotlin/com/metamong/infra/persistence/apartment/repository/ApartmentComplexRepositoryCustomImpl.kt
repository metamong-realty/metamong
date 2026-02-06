package com.metamong.infra.persistence.apartment.repository

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.metamong.support.QuerydslRepositorySupport
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.QApartmentComplexEntity
import com.metamong.infra.persistence.apartment.projection.ApartmentComplexListProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ApartmentComplexRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentComplexEntity::class.java),
    ApartmentComplexRepositoryCustom {
    private val complex = QApartmentComplexEntity.apartmentComplexEntity

    override fun findComplexesByConditions(
        sidoSigunguCode: Int,
        eupmyeondongCode: Int?,
        keyword: String?,
        pageable: Pageable,
    ): Page<ApartmentComplexListProjection> {
        val conditions =
            listOfNotNull(
                complex.sidoSigunguCode.eq(sidoSigunguCode),
                eupmyeondongCondition(eupmyeondongCode),
                keywordCondition(keyword),
            )

        val countQuery =
            queryFactory
                .select(complex.count())
                .from(complex)
                .where(*conditions.toTypedArray())

        val total = countQuery.fetchOne() ?: 0L

        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        ApartmentComplexListProjection::class.java,
                        complex.id,
                        complex.nameRaw,
                        complex.builtYear,
                        complex.totalHousehold,
                        complex.eupmyeondongRiCode,
                        complex.addressJibun,
                    ),
                ).from(complex)
                .where(*conditions.toTypedArray())
                .orderBy(complex.nameRaw.asc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        return PageImpl(content, pageable, total)
    }

    private fun eupmyeondongCondition(eupmyeondongCode: Int?): BooleanExpression? =
        eupmyeondongCode?.let {
            complex.eupmyeondongRiCode.divide(100).eq(it)
        }

    private fun keywordCondition(keyword: String?): BooleanExpression? =
        keyword?.takeIf { it.isNotBlank() }?.let {
            complex.nameRaw
                .containsIgnoreCase(it)
                .or(complex.nameNormalized.containsIgnoreCase(it))
        }

    override fun findDistinctEupmyeondongCodes(sidoSigunguCode: Int): List<Int> =
        queryFactory
            .select(complex.eupmyeondongRiCode.divide(100))
            .from(complex)
            .where(complex.sidoSigunguCode.eq(sidoSigunguCode))
            .distinct()
            .fetch()
            .filterNotNull()
}
package com.metamong.infra.persistence.apartment.repository

import com.metamong.application.apartment.request.SortOrder
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.QApartmentComplexEntity
import com.metamong.domain.apartment.model.QApartmentTradeEntity
import com.metamong.domain.apartment.model.QApartmentUnitTypeEntity
import com.metamong.infra.persistence.apartment.projection.ApartmentComplexListProjection
import com.metamong.support.QuerydslRepositorySupport
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.JPAExpressions
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ApartmentComplexRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentComplexEntity::class.java),
    ApartmentComplexRepositoryCustom {
    private val complex = QApartmentComplexEntity.apartmentComplexEntity

    override fun findComplexesByConditions(
        sidoSigunguCode: Int,
        eupmyeondongCode: Int?,
        keyword: String?,
        sortOrder: SortOrder,
        pageable: Pageable,
    ): Page<ApartmentComplexListProjection> {
        val conditions =
            listOfNotNull(
                sidoSigunguCodeCondition(sidoSigunguCode),
                eupmyeondongCondition(eupmyeondongCode),
                keywordCondition(keyword),
            )

        val countQuery =
            queryFactory
                .select(complex.count())
                .from(complex)
                .where(*conditions.toTypedArray())

        val total = countQuery.fetchOne() ?: 0L

        // 서브쿼리용 엔티티 별칭
        val unitType = QApartmentUnitTypeEntity.apartmentUnitTypeEntity
        val trade = QApartmentTradeEntity.apartmentTradeEntity

        // 현재 연도 계산 (최근 3년 필터용)
        val currentYear = LocalDate.now().year
        val threeYearsAgo = currentYear - 3

        // 전체 거래 건수 서브쿼리 (정렬용)
        val totalTradeCountSubQuery =
            JPAExpressions
                .select(trade.count())
                .from(trade)
                .join(unitType)
                .on(trade.unitTypeId.eq(unitType.id))
                .where(unitType.complexId.eq(complex.id))

        val query =
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
                        // 전체 거래 건수
                        totalTradeCountSubQuery,
                        // 최근 3년 거래 건수
                        JPAExpressions
                            .select(trade.count())
                            .from(trade)
                            .join(unitType)
                            .on(trade.unitTypeId.eq(unitType.id))
                            .where(
                                unitType.complexId.eq(complex.id),
                                trade.contractYear.goe(threeYearsAgo),
                            ),
                    ),
                ).from(complex)
                .where(*conditions.toTypedArray())

        // 정렬 조건 적용
        when (sortOrder) {
            SortOrder.TRADE_COUNT -> query.orderBy(OrderSpecifier(Order.DESC, totalTradeCountSubQuery))
            SortOrder.BUILT_YEAR -> query.orderBy(complex.builtYear.desc())
            SortOrder.DEFAULT -> query.orderBy(complex.nameRaw.asc())
        }

        val content =
            query
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

    private fun sidoSigunguCodeCondition(sidoSigunguCode: Int): BooleanExpression {
        val codeString = sidoSigunguCode.toString()
        return if (codeString.length == 5) {
            // 시군구 레벨 검색 (예: 41170 → 41170, 41171, 41173 모두 매칭)
            complex.sidoSigunguCode.stringValue().startsWith(codeString)
        } else {
            // 정확한 매칭
            complex.sidoSigunguCode.eq(sidoSigunguCode)
        }
    }

    override fun findDistinctSidoCodes(): List<Int> =
        queryFactory
            .select(complex.sidoSigunguCode.divide(1000))
            .from(complex)
            .distinct()
            .fetch()
            .filterNotNull()
            .sorted()

    override fun findDistinctSidoSigunguCodes(): List<Int> =
        queryFactory
            .select(complex.sidoSigunguCode)
            .from(complex)
            .distinct()
            .fetch()
            .filterNotNull()
            .sorted()

    override fun findDistinctEupmyeondongCodes(sidoSigunguCode: Int): List<Int> {
        val condition = sidoSigunguCodeCondition(sidoSigunguCode)
        return queryFactory
            .select(complex.eupmyeondongRiCode.divide(100))
            .from(complex)
            .where(condition)
            .distinct()
            .fetch()
            .filterNotNull()
            .sorted()
    }
}

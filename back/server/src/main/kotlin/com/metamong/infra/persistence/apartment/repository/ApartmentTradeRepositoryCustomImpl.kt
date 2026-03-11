package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.domain.apartment.model.QApartmentTradeEntity
import com.metamong.domain.apartment.model.QApartmentUnitTypeEntity
import com.metamong.infra.persistence.apartment.projection.ApartmentTradeChartProjection
import com.metamong.infra.persistence.apartment.projection.ApartmentTradeListProjection
import com.metamong.support.QuerydslRepositorySupport
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ApartmentTradeRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentTradeEntity::class.java),
    ApartmentTradeRepositoryCustom {
    private val trade = QApartmentTradeEntity.apartmentTradeEntity
    private val unitType = QApartmentUnitTypeEntity.apartmentUnitTypeEntity

    override fun findTradesByConditions(
        complexId: Long,
        unitTypeId: Long?,
        startDate: LocalDate?,
        pageable: Pageable,
    ): Page<ApartmentTradeListProjection> {
        val conditions =
            listOfNotNull(
                // unitTypeId가 제공되면 complexId 조건 불필요 (unitTypeId에 이미 complex 정보 포함)
                if (unitTypeId == null) unitType.complexId.eq(complexId) else null,
                unitTypeIdCondition(unitTypeId),
                startDateCondition(startDate),
            )

        val total =
            queryFactory
                .select(trade.count())
                .from(trade)
                .innerJoin(unitType)
                .on(trade.unitTypeId.eq(unitType.id))
                .where(*conditions.toTypedArray())
                .fetchOne() ?: 0L

        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        ApartmentTradeListProjection::class.java,
                        trade.id,
                        trade.contractDate,
                        trade.exclusiveArea,
                        unitType.exclusivePyeong,
                        trade.floor,
                        trade.price,
                        trade.dealType,
                        trade.isCanceled,
                    ),
                ).from(trade)
                .innerJoin(unitType)
                .on(trade.unitTypeId.eq(unitType.id))
                .where(*conditions.toTypedArray())
                .orderBy(trade.contractDate.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        return PageImpl(content, pageable, total)
    }

    override fun findTradesForChart(
        complexId: Long,
        unitTypeId: Long?,
        startDate: LocalDate?,
    ): List<ApartmentTradeChartProjection> {
        val conditions =
            listOfNotNull(
                // unitTypeId가 제공되면 complexId 조건 불필요 (unitTypeId에 이미 complex 정보 포함)
                if (unitTypeId == null) unitType.complexId.eq(complexId) else null,
                unitTypeIdCondition(unitTypeId),
                startDateCondition(startDate),
                trade.isCanceled.eq(false),
            )

        return queryFactory
            .select(
                Projections.constructor(
                    ApartmentTradeChartProjection::class.java,
                    trade.contractYear,
                    trade.contractMonth,
                    trade.price.avg(),
                    trade.price.max(),
                    trade.price.min(),
                    trade.count(),
                ),
            ).from(trade)
            .innerJoin(unitType)
            .on(trade.unitTypeId.eq(unitType.id))
            .where(*conditions.toTypedArray())
            .groupBy(trade.contractYear, trade.contractMonth)
            .orderBy(trade.contractYear.asc(), trade.contractMonth.asc())
            .fetch()
    }

    private fun unitTypeIdCondition(unitTypeId: Long?): BooleanExpression? = unitTypeId?.let { trade.unitTypeId.eq(it) }

    private fun startDateCondition(startDate: LocalDate?): BooleanExpression? = startDate?.let { trade.contractDate.goe(it) }
}

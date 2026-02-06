package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.QApartmentRentEntity
import com.metamong.domain.apartment.model.QApartmentUnitTypeEntity
import com.metamong.domain.apartment.model.RentType
import com.metamong.infra.persistence.apartment.projection.ApartmentRentChartProjection
import com.metamong.infra.persistence.apartment.projection.ApartmentRentListProjection
import com.metamong.support.QuerydslRepositorySupport
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class ApartmentRentRepositoryCustomImpl :
    QuerydslRepositorySupport(ApartmentRentEntity::class.java),
    ApartmentRentRepositoryCustom {
    private val rent = QApartmentRentEntity.apartmentRentEntity
    private val unitType = QApartmentUnitTypeEntity.apartmentUnitTypeEntity

    override fun findRentsByConditions(
        complexId: Long,
        unitTypeId: Long?,
        rentType: RentType?,
        startDate: LocalDate?,
        pageable: Pageable,
    ): Page<ApartmentRentListProjection> {
        val conditions =
            listOfNotNull(
                unitType.complexId.eq(complexId),
                unitTypeIdCondition(unitTypeId),
                rentTypeCondition(rentType),
                startDateCondition(startDate),
            )

        val total =
            queryFactory
                .select(rent.count())
                .from(rent)
                .innerJoin(unitType)
                .on(rent.unitTypeId.eq(unitType.id))
                .where(*conditions.toTypedArray())
                .fetchOne() ?: 0L

        val content =
            queryFactory
                .select(
                    Projections.constructor(
                        ApartmentRentListProjection::class.java,
                        rent.id,
                        rent.contractDate,
                        unitType.exclusiveArea,
                        unitType.exclusivePyeong,
                        rent.floor,
                        rent.rentType,
                        rent.deposit,
                        rent.monthlyRent,
                        rent.isCanceled,
                    ),
                ).from(rent)
                .innerJoin(unitType)
                .on(rent.unitTypeId.eq(unitType.id))
                .where(*conditions.toTypedArray())
                .orderBy(rent.contractDate.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        return PageImpl(content, pageable, total)
    }

    override fun findRentsForChart(
        complexId: Long,
        unitTypeId: Long?,
        rentType: RentType?,
        startDate: LocalDate?,
    ): List<ApartmentRentChartProjection> {
        val conditions =
            listOfNotNull(
                unitType.complexId.eq(complexId),
                unitTypeIdCondition(unitTypeId),
                rentTypeCondition(rentType),
                startDateCondition(startDate),
                rent.isCanceled.eq(false),
            )

        return queryFactory
            .select(
                Projections.constructor(
                    ApartmentRentChartProjection::class.java,
                    rent.contractYear,
                    rent.contractMonth,
                    rent.deposit.avg(),
                    rent.deposit.max(),
                    rent.deposit.min(),
                    rent.count(),
                ),
            ).from(rent)
            .innerJoin(unitType)
            .on(rent.unitTypeId.eq(unitType.id))
            .where(*conditions.toTypedArray())
            .groupBy(rent.contractYear, rent.contractMonth)
            .orderBy(rent.contractYear.asc(), rent.contractMonth.asc())
            .fetch()
    }

    private fun unitTypeIdCondition(unitTypeId: Long?): BooleanExpression? = unitTypeId?.let { rent.unitTypeId.eq(it) }

    private fun rentTypeCondition(rentType: RentType?): BooleanExpression? = rentType?.let { rent.rentType.eq(it) }

    private fun startDateCondition(startDate: LocalDate?): BooleanExpression? = startDate?.let { rent.contractDate.goe(it) }
}

package com.metamong.infra.persistence.apartment.repository

import com.metamong.infra.persistence.apartment.projection.ApartmentTradeChartProjection
import com.metamong.infra.persistence.apartment.projection.ApartmentTradeListProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ApartmentTradeRepositoryCustom {
    fun findTradesByConditions(
        complexId: Long,
        unitTypeId: Long?,
        startDate: LocalDate?,
        pageable: Pageable,
    ): Page<ApartmentTradeListProjection>

    fun findTradesForChart(
        complexId: Long,
        unitTypeId: Long?,
        startDate: LocalDate?,
    ): List<ApartmentTradeChartProjection>
}

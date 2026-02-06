package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.RentType
import com.metamong.infra.persistence.apartment.projection.ApartmentRentChartProjection
import com.metamong.infra.persistence.apartment.projection.ApartmentRentListProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

interface ApartmentRentRepositoryCustom {
    fun findRentsByConditions(
        complexId: Long,
        unitTypeId: Long?,
        rentType: RentType?,
        startDate: LocalDate?,
        pageable: Pageable,
    ): Page<ApartmentRentListProjection>

    fun findRentsForChart(
        complexId: Long,
        unitTypeId: Long?,
        rentType: RentType?,
        startDate: LocalDate?,
    ): List<ApartmentRentChartProjection>
}
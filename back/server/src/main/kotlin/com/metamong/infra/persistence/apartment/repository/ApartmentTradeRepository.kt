package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentTradeRepository :
    JpaRepository<ApartmentTradeEntity, Long>,
    ApartmentTradeRepositoryCustom {
    fun countByUnitTypeIdIn(unitTypeIds: List<Long>): Long
}

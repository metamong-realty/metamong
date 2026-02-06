package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentRentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentRentRepository :
    JpaRepository<ApartmentRentEntity, Long>,
    ApartmentRentRepositoryCustom {
    fun countByUnitTypeIdIn(unitTypeIds: List<Long>): Long
}

package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentUnitTypeRepository : JpaRepository<ApartmentUnitTypeEntity, Long> {
    fun findByComplexId(complexId: Long): List<ApartmentUnitTypeEntity>

    fun findByComplexIdOrderByExclusiveAreaAsc(complexId: Long): List<ApartmentUnitTypeEntity>
}

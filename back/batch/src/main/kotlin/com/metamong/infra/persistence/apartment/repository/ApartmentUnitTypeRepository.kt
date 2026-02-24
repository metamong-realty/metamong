package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentUnitTypeRepository : JpaRepository<ApartmentUnitTypeEntity, Long> {
    fun findByComplexIdAndExclusivePyeong(
        complexId: Long,
        exclusivePyeong: Short,
    ): ApartmentUnitTypeEntity?

    fun findAllByComplexId(complexId: Long): List<ApartmentUnitTypeEntity>
}

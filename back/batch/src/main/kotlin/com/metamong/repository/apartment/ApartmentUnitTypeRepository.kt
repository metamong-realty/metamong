package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentUnitTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

interface ApartmentUnitTypeRepository : JpaRepository<ApartmentUnitTypeEntity, Long> {
    fun findByComplexIdAndExclusiveArea(
        complexId: Long,
        exclusiveArea: BigDecimal,
    ): ApartmentUnitTypeEntity?

    fun findAllByComplexId(complexId: Long): List<ApartmentUnitTypeEntity>
}

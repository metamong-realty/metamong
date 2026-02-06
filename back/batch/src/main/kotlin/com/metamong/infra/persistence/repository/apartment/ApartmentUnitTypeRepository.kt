package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigDecimal

interface ApartmentUnitTypeRepository : JpaRepository<ApartmentUnitTypeEntity, Long> {
    fun findByComplexIdAndExclusiveArea(
        complexId: Long,
        exclusiveArea: BigDecimal,
    ): ApartmentUnitTypeEntity?

    fun findAllByComplexId(complexId: Long): List<ApartmentUnitTypeEntity>
}

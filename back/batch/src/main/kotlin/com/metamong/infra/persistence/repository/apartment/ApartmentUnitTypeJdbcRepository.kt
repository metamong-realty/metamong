package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity

interface ApartmentUnitTypeJdbcRepository {
    fun batchInsert(entities: List<ApartmentUnitTypeEntity>): List<ApartmentUnitTypeEntity>
}

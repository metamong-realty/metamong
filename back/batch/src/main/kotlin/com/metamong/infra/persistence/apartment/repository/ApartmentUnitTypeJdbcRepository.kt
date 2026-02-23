package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity

interface ApartmentUnitTypeJdbcRepository {
    fun batchInsert(entities: List<ApartmentUnitTypeEntity>): List<ApartmentUnitTypeEntity>
}

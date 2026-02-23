package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity

interface ApartmentCodeMappingJdbcRepository {
    fun batchInsert(entities: List<ApartmentCodeMappingEntity>): Int
}

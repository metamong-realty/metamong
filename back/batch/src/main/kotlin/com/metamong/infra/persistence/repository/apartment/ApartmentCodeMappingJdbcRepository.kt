package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity

interface ApartmentCodeMappingJdbcRepository {
    fun batchInsert(entities: List<ApartmentCodeMappingEntity>): Int
}

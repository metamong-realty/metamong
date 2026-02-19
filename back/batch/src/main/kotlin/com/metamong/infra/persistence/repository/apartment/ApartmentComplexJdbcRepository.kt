package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentComplexEntity

interface ApartmentComplexJdbcRepository {
    fun batchInsert(entities: List<ApartmentComplexEntity>): List<ApartmentComplexEntity>
}

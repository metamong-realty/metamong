package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentComplexEntity

interface ApartmentComplexJdbcRepository {
    fun batchInsert(entities: List<ApartmentComplexEntity>): List<ApartmentComplexEntity>

    fun batchUpdate(entities: List<ApartmentComplexEntity>): Int
}

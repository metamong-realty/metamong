package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentRentEntity

interface ApartmentRentJdbcRepository {
    fun batchUpsert(entities: List<ApartmentRentEntity>): Int
}

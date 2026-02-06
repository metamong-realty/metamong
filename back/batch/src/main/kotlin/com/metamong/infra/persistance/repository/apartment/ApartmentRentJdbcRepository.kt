package com.metamong.infra.persistance.repository.apartment

import com.metamong.domain.apartment.model.ApartmentRentEntity

interface ApartmentRentJdbcRepository {
    fun batchUpsert(entities: List<ApartmentRentEntity>): Int
}

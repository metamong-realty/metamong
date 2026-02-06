package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentRentEntity

interface ApartmentRentJdbcRepository {
    fun batchUpsert(entities: List<ApartmentRentEntity>): Int
}

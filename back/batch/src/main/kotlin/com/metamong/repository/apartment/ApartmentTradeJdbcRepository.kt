package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentTradeEntity

interface ApartmentTradeJdbcRepository {
    fun batchUpsert(entities: List<ApartmentTradeEntity>): Int
}

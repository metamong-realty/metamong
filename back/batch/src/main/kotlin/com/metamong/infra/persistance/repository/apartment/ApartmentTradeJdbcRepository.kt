package com.metamong.infra.persistance.repository.apartment

import com.metamong.domain.apartment.model.ApartmentTradeEntity

interface ApartmentTradeJdbcRepository {
    fun batchUpsert(entities: List<ApartmentTradeEntity>): Int
}

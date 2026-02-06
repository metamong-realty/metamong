package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentTradeEntity

interface ApartmentTradeJdbcRepository {
    fun batchUpsert(entities: List<ApartmentTradeEntity>): Int
}

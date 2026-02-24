package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentTradeEntity

interface ApartmentTradeJdbcRepository {
    fun batchUpsert(entities: List<ApartmentTradeEntity>): Int
}

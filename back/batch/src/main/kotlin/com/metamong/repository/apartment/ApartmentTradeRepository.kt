package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentTradeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentTradeRepository :
    JpaRepository<ApartmentTradeEntity, Long>,
    ApartmentTradeJdbcRepository {
    fun findByRawId(rawId: String): ApartmentTradeEntity?

    fun existsByRawId(rawId: String): Boolean
}

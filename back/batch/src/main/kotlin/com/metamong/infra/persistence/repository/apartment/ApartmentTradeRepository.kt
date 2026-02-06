package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentTradeRepository :
    JpaRepository<ApartmentTradeEntity, Long>,
    ApartmentTradeJdbcRepository {
    fun findByRawId(rawId: String): ApartmentTradeEntity?

    fun existsByRawId(rawId: String): Boolean
}

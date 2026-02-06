package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentRentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentRentRepository :
    JpaRepository<ApartmentRentEntity, Long>,
    ApartmentRentJdbcRepository {
    fun findByRawId(rawId: String): ApartmentRentEntity?

    fun existsByRawId(rawId: String): Boolean
}

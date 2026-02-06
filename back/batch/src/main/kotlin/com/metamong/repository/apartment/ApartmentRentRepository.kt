package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentRentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentRentRepository :
    JpaRepository<ApartmentRentEntity, Long>,
    ApartmentRentJdbcRepository {
    fun findByRawId(rawId: String): ApartmentRentEntity?

    fun existsByRawId(rawId: String): Boolean
}

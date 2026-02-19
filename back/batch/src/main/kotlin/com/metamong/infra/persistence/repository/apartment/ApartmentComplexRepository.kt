package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentComplexRepository :
    JpaRepository<ApartmentComplexEntity, Long>,
    ApartmentComplexRepositoryCustom,
    ApartmentComplexJdbcRepository

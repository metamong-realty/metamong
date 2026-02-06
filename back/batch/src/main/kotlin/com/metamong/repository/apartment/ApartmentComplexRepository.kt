package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentComplexEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentComplexRepository :
    JpaRepository<ApartmentComplexEntity, Long>,
    ApartmentComplexRepositoryCustom

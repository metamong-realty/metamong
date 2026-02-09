package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity
import com.metamong.domain.apartment.model.ApartmentCodeType
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentCodeMappingRepository :
    JpaRepository<ApartmentCodeMappingEntity, Long>,
    ApartmentCodeMappingJdbcRepository {
    fun findByCodeTypeAndCodeValue(
        codeType: ApartmentCodeType,
        codeValue: String,
    ): ApartmentCodeMappingEntity?

    fun existsByCodeTypeAndCodeValue(
        codeType: ApartmentCodeType,
        codeValue: String,
    ): Boolean

    fun findAllByComplexId(complexId: Long): List<ApartmentCodeMappingEntity>
}

package com.metamong.infra.persistence.apartment.repository

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

    fun findAllByCodeTypeAndCodeValueIn(
        codeType: ApartmentCodeType,
        codeValues: Collection<String>,
    ): List<ApartmentCodeMappingEntity>

    fun findAllByCodeType(codeType: ApartmentCodeType): List<ApartmentCodeMappingEntity>
}

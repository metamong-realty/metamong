package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentCodeMappingEntity
import com.metamong.enums.apartment.ApartmentCodeType
import org.springframework.data.jpa.repository.JpaRepository

interface ApartmentCodeMappingRepository : JpaRepository<ApartmentCodeMappingEntity, Long> {
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

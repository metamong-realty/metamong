package com.metamong.domain.apartment.model

import com.metamong.domain.base.ExtendedBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "apartment_code_mappings")
class ApartmentCodeMappingEntity(
    val complexId: Long,
    @Enumerated(EnumType.STRING)
    val codeType: ApartmentCodeType,
    val codeValue: String,
) : ExtendedBaseEntity(){
    companion object {
        fun create(
            complexId: Long,
            codeType: ApartmentCodeType,
            codeValue: String,
        ): ApartmentCodeMappingEntity =
            ApartmentCodeMappingEntity(
                complexId = complexId,
                codeType = codeType,
                codeValue = codeValue,
            )
    }
}
package com.metamong.domain.apartment.model

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "apartment_unit_types")
class ApartmentUnitTypeEntity(
    val complexId: Long,
    val exclusivePyeong: Short,
) : BaseEntity() {
    companion object {
        fun create(
            complexId: Long,
            exclusivePyeong: Short,
        ): ApartmentUnitTypeEntity =
            ApartmentUnitTypeEntity(
                complexId = complexId,
                exclusivePyeong = exclusivePyeong,
            )
    }
}

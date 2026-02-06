package com.metamong.domain.apartment.model

import com.metamong.domain.base.ExtendedBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "apartment_unit_types")
class ApartmentUnitTypeEntity(
    val complexId: Long,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Short? = null,
) : ExtendedBaseEntity(){
    companion object {
        fun create(
            complexId: Long,
            exclusiveArea: BigDecimal,
            exclusivePyeong: Short?,
        ): ApartmentUnitTypeEntity =
            ApartmentUnitTypeEntity(
                complexId = complexId,
                exclusiveArea = exclusiveArea,
                exclusivePyeong = exclusivePyeong,
            )
    }
}
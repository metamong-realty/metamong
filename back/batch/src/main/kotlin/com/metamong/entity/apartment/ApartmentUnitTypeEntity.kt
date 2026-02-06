package com.metamong.entity.apartment

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "apartment_unit_types")
class ApartmentUnitTypeEntity(
    val complexId: Long,
    val exclusiveArea: BigDecimal,
    var exclusivePyeong: Short? = null,
    var supplyArea: BigDecimal? = null,
    var typeName: String? = null,
) : BaseEntity() {
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

package com.metamong.application.apartment.dto

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import java.math.BigDecimal

data class ApartmentUnitTypeDto(
    val unitTypeId: Long,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Int?,
) {
    companion object {
        fun from(entity: ApartmentUnitTypeEntity) =
            ApartmentUnitTypeDto(
                unitTypeId = entity.id!!,
                exclusiveArea = entity.exclusiveArea,
                exclusivePyeong = entity.exclusivePyeong?.toInt(),
            )
    }
}

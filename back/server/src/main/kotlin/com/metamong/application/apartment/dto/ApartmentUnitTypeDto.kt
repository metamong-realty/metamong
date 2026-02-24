package com.metamong.application.apartment.dto

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity

data class ApartmentUnitTypeDto(
    val unitTypeId: Long,
    val exclusivePyeong: Int,
) {
    companion object {
        fun from(entity: ApartmentUnitTypeEntity) =
            ApartmentUnitTypeDto(
                unitTypeId = entity.id,
                exclusivePyeong = entity.exclusivePyeong.toInt(),
            )
    }
}

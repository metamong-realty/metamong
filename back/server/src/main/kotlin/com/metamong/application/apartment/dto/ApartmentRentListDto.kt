package com.metamong.application.apartment.dto

import com.metamong.domain.apartment.model.RentType
import com.metamong.infra.persistence.apartment.projection.ApartmentRentListProjection
import java.math.BigDecimal
import java.time.LocalDate

data class ApartmentRentListDto(
    val rentId: Long,
    val contractDate: LocalDate?,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Int?,
    val floor: Int?,
    val rentType: RentType,
    val deposit: Int,
    val monthlyRent: Int,
    val isCanceled: Boolean,
) {
    companion object {
        fun from(projection: ApartmentRentListProjection) =
            ApartmentRentListDto(
                rentId = projection.id,
                contractDate = projection.contractDate,
                exclusiveArea = projection.exclusiveArea,
                exclusivePyeong = projection.exclusivePyeong?.toInt(),
                floor = projection.floor?.toInt(),
                rentType = projection.rentType,
                deposit = projection.deposit,
                monthlyRent = projection.monthlyRent,
                isCanceled = projection.isCanceled,
            )
    }
}
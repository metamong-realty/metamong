package com.metamong.infra.persistence.apartment.projection

import com.metamong.domain.apartment.model.RentType
import java.math.BigDecimal
import java.time.LocalDate

data class ApartmentRentListProjection(
    val id: Long,
    val contractDate: LocalDate?,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Short?,
    val floor: Short?,
    val rentType: RentType,
    val deposit: Int,
    val monthlyRent: Int,
    val isCanceled: Boolean,
)
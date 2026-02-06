package com.metamong.infrastructure.persistence.apartment.projection

import java.math.BigDecimal
import java.time.LocalDate

data class ApartmentTradeListProjection(
    val id: Long,
    val contractDate: LocalDate?,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Short?,
    val floor: Short?,
    val price: Int,
    val dealType: String?,
    val isCanceled: Boolean,
)
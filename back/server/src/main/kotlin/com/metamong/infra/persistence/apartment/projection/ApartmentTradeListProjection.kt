package com.metamong.infra.persistence.apartment.projection

import java.math.BigDecimal
import java.time.LocalDate

data class ApartmentTradeListProjection(
    val id: Long,
    val contractDate: LocalDate?,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Int?,
    val floor: Int?,
    val price: Int,
    val dealType: String?,
    val isCanceled: Boolean,
)

package com.metamong.application.apartment.dto

import com.metamong.infra.persistence.apartment.projection.ApartmentTradeListProjection
import java.math.BigDecimal
import java.time.LocalDate

data class ApartmentTradeListDto(
    val tradeId: Long,
    val contractDate: LocalDate?,
    val exclusiveArea: BigDecimal,
    val exclusivePyeong: Int?,
    val floor: Int?,
    val price: Int,
    val isDirect: Boolean,
    val isCanceled: Boolean,
) {
    companion object {
        private const val DIRECT_DEAL_TYPE = "직거래"

        fun from(projection: ApartmentTradeListProjection) =
            ApartmentTradeListDto(
                tradeId = projection.id,
                contractDate = projection.contractDate,
                exclusiveArea = projection.exclusiveArea,
                exclusivePyeong = projection.exclusivePyeong?.toInt(),
                floor = projection.floor?.toInt(),
                price = projection.price,
                isDirect = projection.dealType == DIRECT_DEAL_TYPE,
                isCanceled = projection.isCanceled,
            )
    }
}

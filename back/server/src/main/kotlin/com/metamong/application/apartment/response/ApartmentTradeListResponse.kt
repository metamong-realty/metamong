package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentTradeListDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "아파트 매매 내역 응답")
data class ApartmentTradeListResponse(
    @Schema(description = "거래 ID", example = "1")
    val tradeId: Long,
    @Schema(description = "계약일", example = "2024-10-22")
    val contractDate: LocalDate?,
    @Schema(description = "전용면적 (㎡)", example = "24.00")
    val exclusiveArea: BigDecimal,
    @Schema(description = "전용면적 (평)", example = "7")
    val exclusivePyeong: Int?,
    @Schema(description = "층", example = "9")
    val floor: Int?,
    @Schema(description = "거래가격 (만원)", example = "57000")
    val price: Int,
    @Schema(description = "직거래 여부", example = "false")
    val isDirect: Boolean,
    @Schema(description = "취소 여부", example = "false")
    val isCanceled: Boolean,
) {
    companion object {
        fun from(dto: ApartmentTradeListDto) =
            ApartmentTradeListResponse(
                tradeId = dto.tradeId,
                contractDate = dto.contractDate,
                exclusiveArea = dto.exclusiveArea,
                exclusivePyeong = dto.exclusivePyeong,
                floor = dto.floor,
                price = dto.price,
                isDirect = dto.isDirect,
                isCanceled = dto.isCanceled,
            )
    }
}
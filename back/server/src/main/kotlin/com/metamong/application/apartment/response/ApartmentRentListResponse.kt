package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentRentListDto
import com.metamong.domain.apartment.model.RentType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "아파트 전월세 내역 응답")
data class ApartmentRentListResponse(
    @Schema(description = "거래 ID", example = "1")
    val rentId: Long,
    @Schema(description = "계약일", example = "2024-11-26")
    val contractDate: LocalDate?,
    @Schema(description = "전용면적 (㎡)", example = "24.00")
    val exclusiveArea: BigDecimal,
    @Schema(description = "전용면적 (평)", example = "7")
    val exclusivePyeong: Int?,
    @Schema(description = "층", example = "15")
    val floor: Int?,
    @Schema(description = "전월세 구분", example = "JEONSE")
    val rentType: RentType,
    @Schema(description = "보증금 (만원)", example = "28000")
    val deposit: Int,
    @Schema(description = "월세 (만원)", example = "0")
    val monthlyRent: Int,
    @Schema(description = "취소 여부", example = "false")
    val isCanceled: Boolean,
) {
    companion object {
        fun from(dto: ApartmentRentListDto) =
            ApartmentRentListResponse(
                rentId = dto.rentId,
                contractDate = dto.contractDate,
                exclusiveArea = dto.exclusiveArea,
                exclusivePyeong = dto.exclusivePyeong,
                floor = dto.floor,
                rentType = dto.rentType,
                deposit = dto.deposit,
                monthlyRent = dto.monthlyRent,
                isCanceled = dto.isCanceled,
            )
    }
}

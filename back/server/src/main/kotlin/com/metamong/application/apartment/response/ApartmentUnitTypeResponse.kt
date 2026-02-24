package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentUnitTypeDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "아파트 평형 응답")
data class ApartmentUnitTypeResponse(
    @Schema(description = "평형 ID", example = "1")
    val unitTypeId: Long,
    @Schema(description = "전용면적 (평)", example = "7")
    val exclusivePyeong: Int,
) {
    companion object {
        fun from(dto: ApartmentUnitTypeDto) =
            ApartmentUnitTypeResponse(
                unitTypeId = dto.unitTypeId,
                exclusivePyeong = dto.exclusivePyeong,
            )
    }
}

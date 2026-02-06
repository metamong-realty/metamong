package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentComplexListDto
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "아파트 단지 목록 응답")
data class ApartmentComplexListResponse(
    @Schema(description = "단지 ID", example = "1")
    val complexId: Long,
    @Schema(description = "단지명", example = "센트럴센트럴")
    val name: String,
    @Schema(description = "준공연도", example = "2010")
    val builtYear: Int?,
    @Schema(description = "세대수", example = "226")
    val totalHousehold: Int?,
    @Schema(description = "읍면동명", example = "역삼동")
    val eupmyeondongName: String?,
    @Schema(description = "간략 주소", example = "역삼동 123-45")
    val addressShort: String?,
    @Schema(description = "총 거래건수", example = "150")
    val totalTradeCount: Long,
    @Schema(description = "최근 3년 거래건수", example = "39")
    val recent3YearsTradeCount: Long,
) {
    companion object {
        fun from(dto: ApartmentComplexListDto) =
            ApartmentComplexListResponse(
                complexId = dto.complexId,
                name = dto.name,
                builtYear = dto.builtYear,
                totalHousehold = dto.totalHousehold,
                eupmyeondongName = dto.eupmyeondongName,
                addressShort = dto.addressShort,
                totalTradeCount = dto.totalTradeCount,
                recent3YearsTradeCount = dto.recent3YearsTradeCount,
            )
    }
}

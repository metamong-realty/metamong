package com.metamong.application.apartment.response

import com.metamong.application.apartment.dto.ApartmentComplexDetailDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

@Schema(description = "아파트 단지 상세 응답")
data class ApartmentComplexDetailResponse(
    @Schema(description = "단지 ID", example = "1")
    val id: Long,
    @Schema(description = "단지명", example = "센트럴센트럴")
    val name: String,
    @Schema(description = "도로명주소", example = "서울특별시 강남구 역삼동 123")
    val addressRoad: String?,
    @Schema(description = "지번주소", example = "서울특별시 강남구 역삼동 123-45")
    val addressJibun: String?,
    @Schema(description = "준공연도", example = "2010")
    val builtYear: Short?,
    @Schema(description = "세대수", example = "226")
    val totalHousehold: Int?,
    @Schema(description = "동수", example = "5")
    val totalBuilding: Int?,
    @Schema(description = "주차대수", example = "300")
    val totalParking: Int?,
    @Schema(description = "용적률", example = "130.00")
    val floorAreaRatio: BigDecimal?,
    @Schema(description = "건폐율", example = "32.00")
    val buildingCoverageRatio: BigDecimal?,
    @Schema(description = "난방방식", example = "개별난방")
    val heatingType: String?,
    @Schema(description = "구독 여부", example = "true")
    val isSubscribed: Boolean = false,
) {
    companion object {
        fun from(dto: ApartmentComplexDetailDto) =
            ApartmentComplexDetailResponse(
                id = dto.id,
                name = dto.name,
                addressRoad = dto.addressRoad,
                addressJibun = dto.addressJibun,
                builtYear = dto.builtYear,
                totalHousehold = dto.totalHousehold,
                totalBuilding = dto.totalBuilding,
                totalParking = dto.totalParking,
                floorAreaRatio = dto.floorAreaRatio,
                buildingCoverageRatio = dto.buildingCoverageRatio,
                heatingType = dto.heatingType,
                isSubscribed = dto.isSubscribed,
            )
    }
}

package com.metamong.application.apartment.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class ApartmentComplexSearchRequest(
    @field:NotBlank(message = "시도시군구코드는 필수입니다")
    @Schema(description = "시도시군구코드 5자리", example = "11680")
    val sidoSigunguCode: String,
    @Schema(description = "읍면동코드 3자리", example = "101")
    val eupmyeondongCode: String? = null,
    @Schema(description = "검색어 (단지명)", example = "래미안")
    val keyword: String? = null,
)
package com.metamong.application.region.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시도 응답")
data class SidoResponse(
    @Schema(description = "시도 코드", example = "11")
    val code: String,
    @Schema(description = "시도명", example = "서울특별시")
    val name: String,
)

@Schema(description = "시군구 응답")
data class SigunguResponse(
    @Schema(description = "시군구 코드", example = "680")
    val code: String,
    @Schema(description = "시군구명", example = "강남구")
    val name: String,
)

@Schema(description = "읍면동 응답")
data class EupmyeondongResponse(
    @Schema(description = "읍면동 코드", example = "101")
    val code: String,
    @Schema(description = "읍면동명", example = "역삼동")
    val name: String,
)

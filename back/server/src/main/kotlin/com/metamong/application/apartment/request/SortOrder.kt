package com.metamong.application.apartment.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "단지 목록 정렬 기준")
enum class SortOrder {
    @Schema(description = "거래량 많은 순")
    TRADE_COUNT,

    @Schema(description = "건설연도 최신순")
    BUILT_YEAR,

    @Schema(description = "기본 정렬 (이름순)")
    DEFAULT,
}

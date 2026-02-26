package com.metamong.application.region.controller

import com.metamong.application.region.response.EupmyeondongResponse
import com.metamong.application.region.response.SidoResponse
import com.metamong.application.region.response.SigunguResponse
import com.metamong.application.region.service.RegionQueryService
import com.metamong.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/apartments/regions")
@Tag(name = "아파트 지역 조회 API", description = "아파트 검색용 시도/시군구/읍면동 지역 목록 조회 API")
class RegionQueryController(
    private val regionQueryService: RegionQueryService,
) {
    @Operation(summary = "시도 목록 조회", description = "전국 시도 목록을 조회합니다.")
    @GetMapping("/sido")
    fun getSidoList(): ApiResponse<List<SidoResponse>> = ApiResponse.ok(regionQueryService.getSidoList())

    @Operation(summary = "시군구 목록 조회", description = "선택한 시도의 시군구 목록을 조회합니다.")
    @GetMapping("/sigungu")
    fun getSigunguList(
        @Parameter(description = "시도 코드", example = "11")
        @RequestParam sidoCode: String,
    ): ApiResponse<List<SigunguResponse>> = ApiResponse.ok(regionQueryService.getSigunguList(sidoCode))

    @Operation(summary = "읍면동 목록 조회", description = "선택한 시군구의 읍면동 목록을 조회합니다.")
    @GetMapping("/eupmyeondong")
    fun getEupmyeondongList(
        @Parameter(description = "시도 코드", example = "11")
        @RequestParam sidoCode: String,
        @Parameter(description = "시군구 코드", example = "680")
        @RequestParam sigunguCode: String,
    ): ApiResponse<List<EupmyeondongResponse>> = ApiResponse.ok(regionQueryService.getEupmyeondongList(sidoCode, sigunguCode))
}

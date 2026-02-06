package com.metamong.application.apartment.controller

import com.metamong.application.apartment.dto.ApartmentRentChartDto
import com.metamong.application.apartment.dto.ApartmentRentListDto
import com.metamong.application.apartment.dto.ApartmentTradeChartDto
import com.metamong.application.apartment.dto.ApartmentTradeListDto
import com.metamong.application.apartment.dto.ApartmentUnitTypeDto
import com.metamong.application.apartment.request.ApartmentComplexSearchRequest
import com.metamong.application.apartment.request.ApartmentRentSearchRequest
import com.metamong.application.apartment.request.ApartmentTradeSearchRequest
import com.metamong.application.apartment.request.PeriodType
import com.metamong.application.apartment.response.ApartmentComplexDetailResponse
import com.metamong.application.apartment.response.ApartmentComplexListResponse
import com.metamong.application.apartment.response.ApartmentPriceSummaryResponse
import com.metamong.application.apartment.response.ApartmentRentChartResponse
import com.metamong.application.apartment.response.ApartmentRentListResponse
import com.metamong.application.apartment.response.ApartmentTradeChartResponse
import com.metamong.application.apartment.response.ApartmentTradeListResponse
import com.metamong.application.apartment.response.ApartmentUnitTypeResponse
import com.metamong.application.apartment.service.ApartmentComplexQueryService
import com.metamong.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/v1/apartments/complexes")
@Tag(name = "아파트 단지 조회 API", description = "아파트 단지 정보 및 실거래가 조회 API")
class ApartmentComplexQueryController(
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
) {
    @Operation(summary = "아파트 단지 목록 조회", description = "지역 기반으로 아파트 단지 목록을 조회합니다.")
    @GetMapping
    fun getComplexes(
        @Valid @ParameterObject @ModelAttribute request: ApartmentComplexSearchRequest,
        @PageableDefault(page = 0, size = 20) pageable: Pageable,
    ): ApiResponse<Page<ApartmentComplexListResponse>> {
        val result =
            apartmentComplexQueryService.getComplexes(
                sidoSigunguCode = request.sidoSigunguCode.toInt(),
                eupmyeondongCode = request.eupmyeondongCode?.toIntOrNull(),
                keyword = request.keyword,
                pageable = pageable,
            )
        return ApiResponse.ok(result.map { ApartmentComplexListResponse.from(it) })
    }

    @Operation(summary = "아파트 단지 상세 조회", description = "아파트 단지 상세 정보를 조회합니다. 로그인 시 구독 여부도 함께 반환합니다.")
    @GetMapping("/{complexId}")
    fun getComplexDetail(
        @PathVariable complexId: Long,
        @RequestParam(required = false) unitTypeId: Long?,
        // TODO: AuthenticationPrincipal로 현재 사용자 정보 가져오기
        // @AuthenticationPrincipal user: AuthorizedUser?,
    ): ApiResponse<ApartmentComplexDetailResponse> {
        // TODO: 실제 인증 사용자 ID를 전달하도록 수정 필요
        val result = apartmentComplexQueryService.getComplexDetail(complexId, null, unitTypeId)
        return ApiResponse.ok(ApartmentComplexDetailResponse.from(result))
    }

    @Operation(summary = "아파트 평형 목록 조회", description = "아파트 단지의 평형 목록을 조회합니다.")
    @GetMapping("/{complexId}/unit-types")
    fun getUnitTypes(
        @PathVariable complexId: Long,
    ): ApiResponse<List<ApartmentUnitTypeResponse>> {
        val result = apartmentComplexQueryService.getUnitTypes(complexId)
        return ApiResponse.ok(result.map { ApartmentUnitTypeResponse.from(ApartmentUnitTypeDto.from(it)) })
    }

    @Operation(summary = "아파트 가격 요약 조회", description = "아파트 단지의 최근 평균가격과 전월 대비 변동률을 조회합니다.")
    @GetMapping("/{complexId}/price-summary")
    fun getPriceSummary(
        @PathVariable complexId: Long,
        @RequestParam(required = false) unitTypeId: Long?,
        @RequestParam(required = false, defaultValue = "3") lookbackMonths: Int,
    ): ApiResponse<ApartmentPriceSummaryResponse> {
        val result = apartmentComplexQueryService.getPriceSummary(complexId, unitTypeId, lookbackMonths)
        return ApiResponse.ok(
            ApartmentPriceSummaryResponse.of(
                lookbackMonths = lookbackMonths,
                dto = result,
            ),
        )
    }

    @Operation(summary = "아파트 매매 내역 조회", description = "아파트 단지의 매매 거래 내역을 조회합니다.")
    @GetMapping("/{complexId}/trades")
    fun getTrades(
        @PathVariable complexId: Long,
        @ParameterObject @ModelAttribute request: ApartmentTradeSearchRequest,
        @PageableDefault(page = 0, size = 20) pageable: Pageable,
    ): ApiResponse<Page<ApartmentTradeListResponse>> {
        val startDate = calculateStartDate(request.period)
        val result =
            apartmentComplexQueryService.getTrades(
                complexId = complexId,
                unitTypeId = request.unitTypeId,
                startDate = startDate,
                pageable = pageable,
            )
        return ApiResponse.ok(result.map { ApartmentTradeListResponse.from(ApartmentTradeListDto.from(it)) })
    }

    @Operation(summary = "아파트 매매 그래프 데이터 조회", description = "아파트 단지의 매매 그래프 데이터를 조회합니다.")
    @GetMapping("/{complexId}/trades/chart")
    fun getTradeChart(
        @PathVariable complexId: Long,
        @ParameterObject @ModelAttribute request: ApartmentTradeSearchRequest,
    ): ApiResponse<ApartmentTradeChartResponse> {
        val startDate = calculateStartDate(request.period)
        val tradeDtos =
            apartmentComplexQueryService.getTradeChart(
                complexId = complexId,
                unitTypeId = request.unitTypeId,
                startDate = startDate,
            )
        val rentDtos =
            apartmentComplexQueryService.getRentChart(
                complexId = complexId,
                unitTypeId = request.unitTypeId,
                rentType = null,
                startDate = startDate,
            )
        val rentCountMap = rentDtos.associate { ApartmentRentChartDto.from(it).yearMonth to ApartmentRentChartDto.from(it).rentCount }
        return ApiResponse.ok(ApartmentTradeChartResponse.from(tradeDtos.map { ApartmentTradeChartDto.from(it) }, rentCountMap))
    }

    @Operation(summary = "아파트 전월세 내역 조회", description = "아파트 단지의 전월세 거래 내역을 조회합니다.")
    @GetMapping("/{complexId}/rents")
    fun getRents(
        @PathVariable complexId: Long,
        @ParameterObject @ModelAttribute request: ApartmentRentSearchRequest,
        @PageableDefault(page = 0, size = 20) pageable: Pageable,
    ): ApiResponse<Page<ApartmentRentListResponse>> {
        val startDate = calculateStartDate(request.period)
        val result =
            apartmentComplexQueryService.getRents(
                complexId = complexId,
                unitTypeId = request.unitTypeId,
                rentType = request.rentType,
                startDate = startDate,
                pageable = pageable,
            )
        return ApiResponse.ok(result.map { ApartmentRentListResponse.from(ApartmentRentListDto.from(it)) })
    }

    @Operation(summary = "아파트 전월세 그래프 데이터 조회", description = "아파트 단지의 전월세 그래프 데이터를 조회합니다.")
    @GetMapping("/{complexId}/rents/chart")
    fun getRentChart(
        @PathVariable complexId: Long,
        @ParameterObject @ModelAttribute request: ApartmentRentSearchRequest,
    ): ApiResponse<ApartmentRentChartResponse> {
        val startDate = calculateStartDate(request.period)
        val rentDtos =
            apartmentComplexQueryService.getRentChart(
                complexId = complexId,
                unitTypeId = request.unitTypeId,
                rentType = request.rentType,
                startDate = startDate,
            )
        val tradeDtos =
            apartmentComplexQueryService.getTradeChart(
                complexId = complexId,
                unitTypeId = request.unitTypeId,
                startDate = startDate,
            )
        val tradeCountMap = tradeDtos.associate { ApartmentTradeChartDto.from(it).yearMonth to ApartmentTradeChartDto.from(it).tradeCount }
        return ApiResponse.ok(ApartmentRentChartResponse.from(rentDtos.map { ApartmentRentChartDto.from(it) }, tradeCountMap))
    }

    private fun calculateStartDate(period: PeriodType): LocalDate? =
        when (period) {
            PeriodType.RECENT_3YEARS -> LocalDate.now().minusYears(3)
            PeriodType.ALL -> null
        }
}

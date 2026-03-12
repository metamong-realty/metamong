package com.metamong.application.apartment.service

import com.metamong.application.apartment.dto.ApartmentComplexDetailDto
import com.metamong.application.apartment.dto.ApartmentComplexListDto
import com.metamong.application.apartment.dto.ApartmentPriceSummaryDto
import com.metamong.application.apartment.dto.RentPriceSummaryDto
import com.metamong.application.apartment.dto.TradePriceSummaryDto
import com.metamong.domain.apartment.model.RentType
import com.metamong.infra.persistence.apartment.repository.ApartmentComplexRepository
import com.metamong.infra.persistence.apartment.repository.ApartmentRentRepository
import com.metamong.infra.persistence.apartment.repository.ApartmentSubscriptionRepository
import com.metamong.infra.persistence.apartment.repository.ApartmentTradeRepository
import com.metamong.infra.persistence.apartment.repository.ApartmentUnitTypeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ApartmentComplexQueryService(
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
    private val apartmentTradeRepository: ApartmentTradeRepository,
    private val apartmentRentRepository: ApartmentRentRepository,
    private val apartmentSubscriptionRepository: ApartmentSubscriptionRepository,
) {
    fun getComplexes(
        sidoSigunguCode: Int,
        eupmyeondongCode: Int?,
        keyword: String?,
        pageable: Pageable,
    ): Page<ApartmentComplexListDto> {
        val complexes =
            apartmentComplexRepository.findComplexesByConditions(
                sidoSigunguCode = sidoSigunguCode,
                eupmyeondongCode = eupmyeondongCode,
                keyword = keyword,
                pageable = pageable,
            )

        return complexes.map { projection ->
            ApartmentComplexListDto.from(
                projection = projection,
                eupmyeondongName = "읍면동", // TODO: 지역 서비스 연동 필요
                totalTradeCount = 0L, // TODO: 거래 건수 조회 로직 추가
                recent3YearsTradeCount = 0L, // TODO: 최근 3년 거래 건수 조회 로직 추가
            )
        }
    }

    fun getComplexDetail(
        complexId: Long,
        userId: Long?,
        unitTypeId: Long?,
    ): ApartmentComplexDetailDto {
        val complex =
            apartmentComplexRepository
                .findById(complexId)
                .orElseThrow { IllegalArgumentException("단지를 찾을 수 없습니다: $complexId") }

        val isSubscribed =
            userId?.let {
                apartmentSubscriptionRepository.existsByUserIdAndComplexIdAndUnitTypeId(
                    userId = it,
                    complexId = complexId,
                    unitTypeId = unitTypeId,
                )
            } ?: false

        return ApartmentComplexDetailDto(
            id = complex.id,
            name = complex.nameRaw,
            addressRoad = complex.addressRoad,
            addressJibun = complex.addressJibun,
            builtYear = complex.builtYear,
            totalHousehold = complex.totalHousehold,
            totalBuilding = complex.totalBuilding,
            totalParking = complex.totalParking,
            floorAreaRatio = complex.floorAreaRatio,
            buildingCoverageRatio = complex.buildingCoverageRatio,
            heatingType = complex.heatingType,
            isSubscribed = isSubscribed,
        )
    }

    fun getUnitTypes(complexId: Long) = apartmentUnitTypeRepository.findByComplexIdOrderByExclusivePyeongAsc(complexId)

    fun getTrades(
        complexId: Long,
        unitTypeId: Long?,
        startDate: LocalDate?,
        pageable: Pageable,
    ) = apartmentTradeRepository.findTradesByConditions(
        complexId = complexId,
        unitTypeId = unitTypeId,
        startDate = startDate,
        pageable = pageable,
    )

    fun getTradeChart(
        complexId: Long,
        unitTypeId: Long?,
        startDate: LocalDate?,
    ) = apartmentTradeRepository.findTradesForChart(
        complexId = complexId,
        unitTypeId = unitTypeId,
        startDate = startDate,
    )

    fun getRents(
        complexId: Long,
        unitTypeId: Long?,
        rentType: RentType?,
        startDate: LocalDate?,
        pageable: Pageable,
    ) = apartmentRentRepository.findRentsByConditions(
        complexId = complexId,
        unitTypeId = unitTypeId,
        rentType = rentType,
        startDate = startDate,
        pageable = pageable,
    )

    fun getRentChart(
        complexId: Long,
        unitTypeId: Long?,
        rentType: RentType?,
        startDate: LocalDate?,
    ) = apartmentRentRepository.findRentsForChart(
        complexId = complexId,
        unitTypeId = unitTypeId,
        rentType = rentType,
        startDate = startDate,
    )

    fun getPriceSummary(
        complexId: Long,
        unitTypeId: Long?,
        lookbackMonths: Int,
    ): ApartmentPriceSummaryDto {
        val now = LocalDate.now()
        val recentStartDate = now.minusMonths(1)
        val lookbackStartDate = now.minusMonths(lookbackMonths.toLong() + 1)
        val lookbackEndDate = now.minusMonths(lookbackMonths.toLong())

        // 최근 1개월 매매 데이터
        val recentTrades = apartmentTradeRepository.findTradesForChart(
            complexId = complexId,
            unitTypeId = unitTypeId,
            startDate = recentStartDate,
        )

        // lookback 기준 1개월 매매 데이터
        val lookbackTrades = apartmentTradeRepository.findTradesForChart(
            complexId = complexId,
            unitTypeId = unitTypeId,
            startDate = lookbackStartDate,
        ).filter { it.contractYear * 12 + it.contractMonth < lookbackEndDate.year * 12 + lookbackEndDate.monthValue }

        // 최근 1개월 전월세 데이터
        val recentRents = apartmentRentRepository.findRentsForChart(
            complexId = complexId,
            unitTypeId = unitTypeId,
            rentType = null,
            startDate = recentStartDate,
        )

        // lookback 기준 1개월 전월세 데이터
        val lookbackRents = apartmentRentRepository.findRentsForChart(
            complexId = complexId,
            unitTypeId = unitTypeId,
            rentType = null,
            startDate = lookbackStartDate,
        ).filter { it.contractYear * 12 + it.contractMonth < lookbackEndDate.year * 12 + lookbackEndDate.monthValue }

        val tradeSummary = if (recentTrades.isNotEmpty() && lookbackTrades.isNotEmpty()) {
            val recentAvg = recentTrades.mapNotNull { it.avgPrice }.average().toLong()
            val lookbackAvg = lookbackTrades.mapNotNull { it.avgPrice }.average().toLong()
            val changeRate = if (lookbackAvg > 0) {
                ((recentAvg - lookbackAvg).toBigDecimal().divide(lookbackAvg.toBigDecimal(), 4, java.math.RoundingMode.HALF_UP) * 100.toBigDecimal())
            } else null

            TradePriceSummaryDto(
                recentMonthAvgPrice = recentAvg,
                lookbackMonthAvgPrice = lookbackAvg,
                priceChangeRate = changeRate,
            )
        } else null

        val rentSummary = if (recentRents.isNotEmpty() && lookbackRents.isNotEmpty()) {
            val recentAvg = recentRents.mapNotNull { it.avgDeposit }.average().toLong()
            val lookbackAvg = lookbackRents.mapNotNull { it.avgDeposit }.average().toLong()
            val changeRate = if (lookbackAvg > 0) {
                ((recentAvg - lookbackAvg).toBigDecimal().divide(lookbackAvg.toBigDecimal(), 4, java.math.RoundingMode.HALF_UP) * 100.toBigDecimal())
            } else null

            RentPriceSummaryDto(
                recentMonthAvgDeposit = recentAvg,
                lookbackMonthAvgDeposit = lookbackAvg,
                depositChangeRate = changeRate,
            )
        } else null

        return ApartmentPriceSummaryDto(
            trade = tradeSummary,
            rent = rentSummary,
        )
    }
}

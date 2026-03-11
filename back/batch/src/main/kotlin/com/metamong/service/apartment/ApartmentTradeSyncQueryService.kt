package com.metamong.service.apartment

import com.metamong.batch.jobs.publicdata.sync.cache.InMemoryMigrationCache
import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.domain.apartment.model.RentType
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.util.apartment.AreaConverter
import com.metamong.util.apartment.PriceParser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class ApartmentTradeSyncQueryService(
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
    private val inMemoryMigrationCache: InMemoryMigrationCache,
) {
    fun buildTradeFromRaw(tradeRaw: ApartmentTradeRawDocumentEntity): ApartmentTradeEntity? {
        val rawId = tradeRaw.id ?: return null
        val unitTypeInfo = resolveUnitType(tradeRaw.aptSeq, tradeRaw.excluUseAr, rawId) ?: return null
        val contractInfo = parseContractInfo(tradeRaw.dealYear, tradeRaw.dealMonth, tradeRaw.dealDay) ?: return null

        return ApartmentTradeEntity.create(
            unitTypeId = unitTypeInfo.unitTypeId,
            exclusiveArea = unitTypeInfo.exclusiveArea,
            price = PriceParser.parsePriceOrZero(tradeRaw.dealAmount),
            floor = tradeRaw.floor?.toIntOrNull(),
            contractYear = contractInfo.year,
            contractMonth = contractInfo.month,
            contractDay = contractInfo.day,
            contractDate = contractInfo.date,
            dealType = tradeRaw.dealingGbn?.trim()?.takeIf { it.isNotBlank() },
            isCanceled = tradeRaw.cdealType?.trim()?.isNotBlank() == true,
            canceledDate = parseCanceledDate(tradeRaw.cdealDay),
            rawId = rawId,
        )
    }

    fun buildRentFromRaw(rentRaw: ApartmentRentRawDocumentEntity): ApartmentRentEntity? {
        val rawId = rentRaw.id ?: return null
        val unitTypeInfo = resolveUnitType(rentRaw.aptSeq, rentRaw.excluUseAr, rawId) ?: return null
        val contractInfo = parseContractInfo(rentRaw.dealYear, rentRaw.dealMonth, rentRaw.dealDay) ?: return null

        val deposit = PriceParser.parsePriceOrZero(rentRaw.deposit)
        val monthlyRent = PriceParser.parsePriceOrZero(rentRaw.monthlyRent)

        return ApartmentRentEntity.create(
            unitTypeId = unitTypeInfo.unitTypeId,
            exclusiveArea = unitTypeInfo.exclusiveArea,
            rentType = RentType.fromMonthlyRent(monthlyRent),
            deposit = deposit,
            monthlyRent = monthlyRent,
            floor = rentRaw.floor?.toIntOrNull(),
            contractYear = contractInfo.year,
            contractMonth = contractInfo.month,
            contractDay = contractInfo.day,
            contractDate = contractInfo.date,
            isCanceled = false,
            canceledDate = null,
            rawId = rawId,
        )
    }

    private data class UnitTypeInfo(
        val unitTypeId: Long,
        val exclusiveArea: BigDecimal,
    )

    private fun resolveUnitType(
        apartmentSequence: String?,
        exclusiveUseArea: String?,
        rawId: String,
    ): UnitTypeInfo? {
        if (apartmentSequence == null) return null

        val complexId =
            inMemoryMigrationCache.getComplexId(apartmentSequence)
                ?: apartmentComplexQueryService.getComplexIdByApartmentSequence(apartmentSequence)
        if (complexId == null) {
            logger.warn { "Complex 없음: aptSeq=$apartmentSequence, rawId=$rawId" }
            return null
        }

        val exclusiveArea = AreaConverter.parseExclusiveArea(exclusiveUseArea)
        if (exclusiveArea == null) {
            logger.warn { "전용면적 파싱 실패: excluUseAr=$exclusiveUseArea" }
            return null
        }

        val exclusivePyeong = AreaConverter.toPyeong(exclusiveArea)
        if (exclusivePyeong == null) {
            logger.warn { "평형 변환 실패: exclusiveArea=$exclusiveArea" }
            return null
        }

        val unitTypeId = inMemoryMigrationCache.getUnitTypeId(complexId, exclusivePyeong)
        if (unitTypeId != null) return UnitTypeInfo(unitTypeId, exclusiveArea)

        val unitType = apartmentComplexQueryService.getUnitType(complexId, exclusivePyeong)
        if (unitType == null) {
            logger.warn { "UnitType 없음: complexId=$complexId, pyeong=$exclusivePyeong, rawId=$rawId" }
            return null
        }
        return UnitTypeInfo(unitType.id, exclusiveArea)
    }

    private fun parseContractInfo(
        dealYear: String?,
        dealMonth: String?,
        dealDay: String?,
    ): ContractInfo? {
        val year = dealYear?.toIntOrNull()
        val month = dealMonth?.toIntOrNull()
        if (year == null || month == null) {
            logger.warn { "계약 연월 파싱 실패: dealYear=$dealYear, dealMonth=$dealMonth" }
            return null
        }
        val day = dealDay?.toIntOrNull()
        val date = buildContractDate(year, month, day)
        return ContractInfo(year, month, day, date)
    }

    private data class ContractInfo(
        val year: Int,
        val month: Int,
        val day: Int?,
        val date: LocalDate?,
    )

    private fun buildContractDate(
        year: Int,
        month: Int,
        day: Int?,
    ): LocalDate? {
        if (day == null || day < 1 || day > 31) return null
        return try {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 공공데이터 API 취소일(cdealDay) 파싱
     * 포맷: "YY.MM.DD" (예: "25.01.15" → 2025-01-15)
     */
    private fun parseCanceledDate(cdealDay: String?): LocalDate? {
        if (cdealDay.isNullOrBlank()) return null

        val cleaned = cdealDay.trim().replace(".", "")
        if (cleaned.length != 6) {
            logger.debug { "취소일 포맷 불일치: cdealDay=$cdealDay" }
            return null
        }

        return try {
            val year = 2000 + cleaned.substring(0, 2).toInt()
            val month = cleaned.substring(2, 4).toInt()
            val day = cleaned.substring(4, 6).toInt()
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            logger.debug { "취소일 파싱 실패: cdealDay=$cdealDay" }
            null
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

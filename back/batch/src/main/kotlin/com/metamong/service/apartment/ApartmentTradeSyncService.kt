package com.metamong.service.apartment

import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.domain.apartment.model.RentType
import com.metamong.infra.persistance.repository.apartment.ApartmentRentRepository
import com.metamong.infra.persistance.repository.apartment.ApartmentTradeRepository
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.util.apartment.AreaConverter
import com.metamong.util.apartment.PriceParser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class ApartmentTradeSyncService(
    private val apartmentTradeRepository: ApartmentTradeRepository,
    private val apartmentRentRepository: ApartmentRentRepository,
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
    private val apartmentComplexCommandService: ApartmentComplexCommandService,
) {
    fun buildTradeFromRaw(tradeRaw: ApartmentTradeRawDocumentEntity): ApartmentTradeEntity? {
        val rawId = tradeRaw.id ?: return null
        val unitTypeId = getUnitTypeId(tradeRaw.aptSeq, tradeRaw.excluUseAr, rawId) ?: return null
        val contractInfo = parseContractInfo(tradeRaw.dealYear, tradeRaw.dealMonth, tradeRaw.dealDay) ?: return null

        return ApartmentTradeEntity.create(
            unitTypeId = unitTypeId,
            price = PriceParser.parsePriceOrZero(tradeRaw.dealAmount),
            floor = tradeRaw.floor?.toShortOrNull(),
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
        val unitTypeId = getUnitTypeId(rentRaw.aptSeq, rentRaw.excluUseAr, rawId) ?: return null
        val contractInfo = parseContractInfo(rentRaw.dealYear, rentRaw.dealMonth, rentRaw.dealDay) ?: return null

        val deposit = PriceParser.parsePriceOrZero(rentRaw.deposit)
        val monthlyRent = PriceParser.parsePriceOrZero(rentRaw.monthlyRent)

        return ApartmentRentEntity.create(
            unitTypeId = unitTypeId,
            rentType = RentType.fromMonthlyRent(monthlyRent),
            deposit = deposit,
            monthlyRent = monthlyRent,
            floor = rentRaw.floor?.toShortOrNull(),
            contractYear = contractInfo.year,
            contractMonth = contractInfo.month,
            contractDay = contractInfo.day,
            contractDate = contractInfo.date,
            isCanceled = false,
            canceledDate = null,
            rawId = rawId,
        )
    }

    private fun getUnitTypeId(
        apartmentSequence: String?,
        exclusiveUseArea: String?,
        rawId: String,
    ): Long? {
        if (apartmentSequence == null) return null

        val complexId = apartmentComplexQueryService.getComplexIdByApartmentSequence(apartmentSequence)
        if (complexId == null) {
            logger.warn { "Complex 없음: aptSeq=$apartmentSequence, rawId=$rawId" }
            return null
        }

        val exclusiveArea = AreaConverter.parseExclusiveArea(exclusiveUseArea)
        if (exclusiveArea == null) {
            logger.warn { "전용면적 파싱 실패: excluUseAr=$exclusiveUseArea" }
            return null
        }

        return apartmentComplexCommandService.createOrGetUnitType(complexId, exclusiveArea).id
    }

    private fun parseContractInfo(
        dealYear: String?,
        dealMonth: String?,
        dealDay: String?,
    ): ContractInfo? {
        val year = dealYear?.toShortOrNull()
        val month = dealMonth?.toShortOrNull()
        if (year == null || month == null) {
            logger.warn { "계약 연월 파싱 실패: dealYear=$dealYear, dealMonth=$dealMonth" }
            return null
        }
        val day = dealDay?.toShortOrNull()
        val date = buildContractDate(year, month, day)
        return ContractInfo(year, month, day, date)
    }

    private data class ContractInfo(
        val year: Short,
        val month: Short,
        val day: Short?,
        val date: LocalDate?,
    )

    fun batchUpsertTrades(trades: List<ApartmentTradeEntity>): Int = apartmentTradeRepository.batchUpsert(trades)

    fun batchUpsertRents(rents: List<ApartmentRentEntity>): Int = apartmentRentRepository.batchUpsert(rents)

    private fun buildContractDate(
        year: Short,
        month: Short,
        day: Short?,
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

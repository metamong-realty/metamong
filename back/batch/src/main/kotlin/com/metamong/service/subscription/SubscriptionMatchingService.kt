package com.metamong.service.subscription

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.domain.subscription.model.NotificationEventEntity
import com.metamong.domain.subscription.model.SubscriptionEntity
import com.metamong.infra.persistence.repository.apartment.ApartmentComplexRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentUnitTypeRepository
import com.metamong.infra.persistence.subscription.repository.NotificationEventRepository
import com.metamong.infra.persistence.subscription.repository.SubscriptionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional(readOnly = true)
class SubscriptionMatchingService(
    private val subscriptionRepository: SubscriptionRepository,
    private val apartmentUnitTypeRepository: ApartmentUnitTypeRepository,
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val notificationEventRepository: NotificationEventRepository,
) {
    fun matchTrades(trades: List<ApartmentTradeEntity>): List<NotificationEventEntity> {
        if (trades.isEmpty()) return emptyList()

        val tradeIds = trades.map { it.id }
        val unitTypeIds = trades.map { it.unitTypeId }.distinct()

        val unitTypeMap = apartmentUnitTypeRepository.findAllById(unitTypeIds).associateBy { it.id }
        val complexIds = unitTypeMap.values.map { it.complexId }.distinct()
        val complexMap = apartmentComplexRepository.findAllById(complexIds).associateBy { it.id }

        val allRegionCodes =
            complexMap.values
                .flatMap { complex ->
                    val regionCode = toRegionCode(complex)
                    buildRegionCodeVariants(regionCode)
                }.distinct()

        val complexSubscriptions = complexIds.flatMap { subscriptionRepository.findActiveByComplexId(it) }
        val regionSubscriptions =
            if (allRegionCodes.isNotEmpty()) {
                subscriptionRepository.findActiveByRegionCodes(allRegionCodes)
            } else {
                emptyList()
            }

        val existingEvents = notificationEventRepository.findAllByTradeIdIn(tradeIds)
        val existingEventKeys = existingEvents.map { it.subscriptionId to it.tradeId }.toSet()

        return trades.flatMap { trade ->
            matchSingleTrade(
                trade = trade,
                unitTypeMap = unitTypeMap,
                complexMap = complexMap,
                complexSubscriptions = complexSubscriptions,
                regionSubscriptions = regionSubscriptions,
                existingEventKeys = existingEventKeys,
            )
        }
    }

    private fun matchSingleTrade(
        trade: ApartmentTradeEntity,
        unitTypeMap: Map<Long, ApartmentUnitTypeEntity>,
        complexMap: Map<Long, ApartmentComplexEntity>,
        complexSubscriptions: List<SubscriptionEntity>,
        regionSubscriptions: List<SubscriptionEntity>,
        existingEventKeys: Set<Pair<Long, Long>>,
    ): List<NotificationEventEntity> {
        val tradeId = trade.id
        val unitType = unitTypeMap[trade.unitTypeId] ?: return emptyList()
        val complex = complexMap[unitType.complexId] ?: return emptyList()

        val regionCode = toRegionCode(complex)
        val regionCodes = buildRegionCodeVariants(regionCode)

        val matchedComplex = complexSubscriptions.filter { it.apartmentComplexId == unitType.complexId }
        val matchedRegion = regionSubscriptions.filter { it.regionCode in regionCodes }
        val conditionSubscriptions =
            subscriptionRepository.findActiveConditionByRegionCodes(
                regionCodes,
                unitType.exclusiveArea,
                BigDecimal.valueOf(trade.price.toLong()),
            )

        val allSubscriptions = matchedComplex + matchedRegion + conditionSubscriptions

        val deduplicatedByUser =
            allSubscriptions
                .groupBy { it.userId }
                .mapValues { (_, subs) ->
                    subs.minByOrNull { it.type.priority }
                        ?: error("구독 목록이 비어있을 수 없습니다")
                }.values

        return deduplicatedByUser.mapNotNull { sub ->
            if ((sub.id to tradeId) in existingEventKeys) {
                null
            } else {
                NotificationEventEntity(
                    userId = sub.userId,
                    subscriptionId = sub.id,
                    tradeId = tradeId,
                )
            }
        }
    }

    private fun toRegionCode(complex: ApartmentComplexEntity): String {
        val sidoSigungu = complex.sidoSigunguCode.toString().padStart(5, '0')
        val eupmyeondongRi = complex.eupmyeondongRiCode?.toString()?.padStart(5, '0') ?: "00000"
        return "$sidoSigungu$eupmyeondongRi"
    }

    private fun buildRegionCodeVariants(regionCode: String): List<String> {
        val sigunguLevel = regionCode.substring(0, 5) + "00000"
        return if (regionCode == sigunguLevel) {
            listOf(regionCode)
        } else {
            listOf(regionCode, sigunguLevel)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

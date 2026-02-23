package com.metamong.service.apartment

import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.infra.persistence.apartment.repository.ApartmentRentRepository
import com.metamong.infra.persistence.apartment.repository.ApartmentTradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ApartmentTradeSyncCommandService(
    private val apartmentTradeRepository: ApartmentTradeRepository,
    private val apartmentRentRepository: ApartmentRentRepository,
) {
    fun batchUpsertTrades(trades: List<ApartmentTradeEntity>): Int = apartmentTradeRepository.batchUpsert(trades)

    fun batchUpsertRents(rents: List<ApartmentRentEntity>): Int = apartmentRentRepository.batchUpsert(rents)
}

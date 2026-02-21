package com.metamong.service.apartment

import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.infra.persistence.repository.apartment.ApartmentRentRepository
import com.metamong.infra.persistence.repository.apartment.ApartmentTradeRepository
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

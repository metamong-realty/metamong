package com.metamong.domain.apartment.model

import com.metamong.domain.base.ExtendedBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "apartment_trades")
class ApartmentTradeEntity(
    val unitTypeId: Long,
    val price: Int,
    val floor: Short? = null,
    val contractYear: Short,
    val contractMonth: Short,
    val contractDay: Short? = null,
    val contractDate: LocalDate? = null,
    val dealType: String? = null,
    val isCanceled: Boolean = false,
    val canceledDate: LocalDate? = null,
    val rawId: String? = null,
) : ExtendedBaseEntity(){
    companion object {
        fun create(
            unitTypeId: Long,
            price: Int,
            floor: Short?,
            contractYear: Short,
            contractMonth: Short,
            contractDay: Short?,
            contractDate: LocalDate?,
            dealType: String?,
            isCanceled: Boolean,
            canceledDate: LocalDate?,
            rawId: String,
        ): ApartmentTradeEntity =
            ApartmentTradeEntity(
                unitTypeId = unitTypeId,
                price = price,
                floor = floor,
                contractYear = contractYear,
                contractMonth = contractMonth,
                contractDay = contractDay,
                contractDate = contractDate,
                dealType = dealType,
                isCanceled = isCanceled,
                canceledDate = canceledDate,
                rawId = rawId,
            )
    }
}
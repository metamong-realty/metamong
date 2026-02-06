package com.metamong.entity.apartment

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "apartment_trades")
class ApartmentTradeEntity(
    val unitTypeId: Long,
    var price: Int,
    var floor: Short? = null,
    val contractYear: Short,
    val contractMonth: Short,
    var contractDay: Short? = null,
    var contractDate: LocalDate? = null,
    var dealType: String? = null,
    var isCanceled: Boolean = false,
    var canceledDate: LocalDate? = null,
    val rawId: String,
) : BaseEntity() {
    fun updateFromRaw(
        price: Int,
        floor: Short?,
        contractDay: Short?,
        contractDate: LocalDate?,
        dealType: String?,
        isCanceled: Boolean,
        canceledDate: LocalDate?,
    ) {
        this.price = price
        this.floor = floor
        this.contractDay = contractDay
        this.contractDate = contractDate
        this.dealType = dealType
        this.isCanceled = isCanceled
        this.canceledDate = canceledDate
    }

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

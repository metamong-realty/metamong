package com.metamong.entity.apartment

import com.metamong.domain.base.BaseEntity
import com.metamong.enums.apartment.RentType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "apartment_rents")
class ApartmentRentEntity(
    val unitTypeId: Long,
    @Enumerated(EnumType.STRING)
    val rentType: RentType,
    var deposit: Int,
    var monthlyRent: Int = 0,
    var floor: Short? = null,
    val contractYear: Short,
    val contractMonth: Short,
    var contractDay: Short? = null,
    var contractDate: LocalDate? = null,
    var isCanceled: Boolean = false,
    var canceledDate: LocalDate? = null,
    val rawId: String,
) : BaseEntity() {
    fun updateFromRaw(
        deposit: Int,
        monthlyRent: Int,
        floor: Short?,
        contractDay: Short?,
        contractDate: LocalDate?,
        isCanceled: Boolean,
        canceledDate: LocalDate?,
    ) {
        this.deposit = deposit
        this.monthlyRent = monthlyRent
        this.floor = floor
        this.contractDay = contractDay
        this.contractDate = contractDate
        this.isCanceled = isCanceled
        this.canceledDate = canceledDate
    }

    companion object {
        fun create(
            unitTypeId: Long,
            rentType: RentType,
            deposit: Int,
            monthlyRent: Int,
            floor: Short?,
            contractYear: Short,
            contractMonth: Short,
            contractDay: Short?,
            contractDate: LocalDate?,
            isCanceled: Boolean,
            canceledDate: LocalDate?,
            rawId: String,
        ): ApartmentRentEntity =
            ApartmentRentEntity(
                unitTypeId = unitTypeId,
                rentType = rentType,
                deposit = deposit,
                monthlyRent = monthlyRent,
                floor = floor,
                contractYear = contractYear,
                contractMonth = contractMonth,
                contractDay = contractDay,
                contractDate = contractDate,
                isCanceled = isCanceled,
                canceledDate = canceledDate,
                rawId = rawId,
            )
    }
}

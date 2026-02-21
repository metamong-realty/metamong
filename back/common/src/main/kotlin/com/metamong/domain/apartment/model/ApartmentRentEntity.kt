package com.metamong.domain.apartment.model

import com.metamong.domain.base.BaseEntity
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
    val deposit: Int,
    val monthlyRent: Int = 0,
    val floor: Short? = null,
    val contractYear: Short,
    val contractMonth: Short,
    val contractDay: Short? = null,
    val contractDate: LocalDate? = null,
    val isCanceled: Boolean = false,
    val canceledDate: LocalDate? = null,
    val rawId: String? = null,
) : BaseEntity() {
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

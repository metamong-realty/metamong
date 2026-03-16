package com.metamong.domain.apartment.model

import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "apartment_complexes")
class ApartmentComplexEntity(
    val sidoSigunguCode: Int,
    var addressRoad: String? = null,
    var addressJibun: String? = null,
    var eupmyeondongRiCode: Int? = null,
    @Enumerated(EnumType.STRING)
    var platType: PlatType = PlatType.LAND,
    var bonNo: Int? = null,
    var buNo: Int? = null,
    var nameRaw: String,
    var nameNormalized: String? = null,
    var builtYear: Int? = null,
    var totalHousehold: Int? = null,
    var totalBuilding: Int? = null,
    var totalParking: Int? = null,
    var floorAreaRatio: BigDecimal? = null,
    var buildingCoverageRatio: BigDecimal? = null,
    var heatingType: String? = null,
) : BaseEntity() {
    fun updateFromLicenseRaw(
        floorAreaRatio: BigDecimal?,
        buildingCoverageRatio: BigDecimal?,
    ) {
        this.floorAreaRatio = floorAreaRatio
        this.buildingCoverageRatio = buildingCoverageRatio
    }

    fun updateFromInfoRaw(
        eupmyeondongRiCode: Int?,
        addressRoad: String?,
        addressJibun: String?,
        bonNo: Int?,
        buNo: Int?,
        totalHousehold: Int?,
        totalBuilding: Int?,
        heatingType: String?,
    ) {
        this.eupmyeondongRiCode = eupmyeondongRiCode
        this.addressRoad = addressRoad
        this.addressJibun = addressJibun
        this.bonNo = bonNo
        this.buNo = buNo
        this.totalHousehold = totalHousehold
        this.totalBuilding = totalBuilding
        this.heatingType = heatingType
    }

    companion object {
        fun create(
            sidoSigunguCode: Int,
            nameRaw: String,
            nameNormalized: String?,
            builtYear: Int?,
            bonNo: Int?,
            buNo: Int?,
            addressRoad: String?,
            addressJibun: String?,
        ): ApartmentComplexEntity =
            ApartmentComplexEntity(
                sidoSigunguCode = sidoSigunguCode,
                nameRaw = nameRaw,
                nameNormalized = nameNormalized,
                builtYear = builtYear,
                bonNo = bonNo,
                buNo = buNo,
                addressRoad = addressRoad,
                addressJibun = addressJibun,
            )
    }
}

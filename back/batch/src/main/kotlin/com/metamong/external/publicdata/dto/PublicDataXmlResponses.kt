package com.metamong.external.publicdata.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

/**
 * 아파트 매매 실거래가 XML 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
data class ApartmentTradeXmlResponse(
    val header: XmlHeader? = null,
    val body: ApartmentTradeBody? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentTradeBody(
    val items: ApartmentTradeItems? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentTradeItems(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val item: List<ApartmentTradeItem>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentTradeItem(
    val dealYear: String? = null,
    val dealMonth: String? = null,
    val dealDay: String? = null,
    val aptNm: String? = null,
    val aptSeq: String? = null,
    val excluUseAr: String? = null,
    val dealAmount: String? = null,
    val floor: String? = null,
    val buildYear: String? = null,
    val roadNm: String? = null,
    val roadNmBonbun: String? = null,
    val roadNmBubun: String? = null,
    val umdNm: String? = null,
    val jibun: String? = null,
    val cdealType: String? = null,
    val cdealDay: String? = null,
    val dealingGbn: String? = null,
    val rgstDate: String? = null,
    val buyerGbn: String? = null,
    val slerGbn: String? = null,
    val estateAgentSggNm: String? = null,
)

/**
 * 아파트 전월세 실거래가 XML 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
data class ApartmentRentXmlResponse(
    val header: XmlHeader? = null,
    val body: ApartmentRentBody? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentRentBody(
    val items: ApartmentRentItems? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentRentItems(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val item: List<ApartmentRentItem>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentRentItem(
    val dealYear: String? = null,
    val dealMonth: String? = null,
    val dealDay: String? = null,
    val aptNm: String? = null,
    val aptSeq: String? = null,
    val excluUseAr: String? = null,
    val deposit: String? = null,
    val monthlyRent: String? = null,
    val floor: String? = null,
    val buildYear: String? = null,
    val roadNm: String? = null,
    val umdNm: String? = null,
    val jibun: String? = null,
    val rentGbn: String? = null,
    val contractGbn: String? = null,
    val contractTerm: String? = null,
    val preDeposit: String? = null,
    val preMonthlyRent: String? = null,
)

/**
 * 공동주택 단지 목록 XML 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
data class ApartmentComplexListXmlResponse(
    val header: XmlHeader? = null,
    val body: ApartmentComplexListBody? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexListBody(
    val items: ApartmentComplexListItems? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexListItems(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val item: List<ApartmentComplexListItem>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexListItem(
    val kaptCode: String? = null,
    val kaptName: String? = null,
    val sidoCd: String? = null,
    val sigunguCd: String? = null,
    val bjdCode: String? = null,
    val kaptAddr: String? = null,
    val kaptTarea: String? = null,
    val kaptDongCnt: String? = null,
    val kaptMparea: String? = null,
    val kaptBuildYear: String? = null,
    val kaptMgmMode: String? = null,
)

/**
 * 공동주택 기본 정보 XML 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
data class ApartmentComplexInfoXmlResponse(
    val header: XmlHeader? = null,
    val body: ApartmentComplexInfoBody? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexInfoBody(
    val item: ApartmentComplexInfoItem? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApartmentComplexInfoItem(
    val kaptCode: String? = null,
    val kaptName: String? = null,
    val kaptAddr: String? = null,
    val bjdAddr: String? = null,
    val kaptTarea: String? = null,
    val kaptDongCnt: String? = null,
    val kaptLowestFloor: String? = null,
    val kaptHighestFloor: String? = null,
    val kaptHeatType: String? = null,
    val kaptHalType: String? = null,
    val kaptMparea: String? = null,
    val kaptMpareaPer: String? = null,
    val kaptMgmMode: String? = null,
    val kaptFloorAreaRatio: String? = null,
    val kaptBuildingCoverageRatio: String? = null,
    val kaptBuildYear: String? = null,
    val kaptUseDate: String? = null,
    val kaptConstructor: String? = null,
    val kaptCompany: String? = null,
    val kaptdTel: String? = null,
    val kaptdFax: String? = null,
    val kaptUrl: String? = null,
    val kaptChargeFee: String? = null,
    val kaptAreaInfo: String? = null,
)

/**
 * 주택인허가 정보 XML 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
data class HousingLicenseXmlResponse(
    val header: XmlHeader? = null,
    val body: HousingLicenseBody? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class HousingLicenseBody(
    val items: HousingLicenseItems? = null,
    val numOfRows: Int? = null,
    val pageNo: Int? = null,
    val totalCount: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class HousingLicenseItems(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val item: List<HousingLicenseItem>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class HousingLicenseItem(
    val mgmHsrgstPk: String? = null,
    val platPlc: String? = null,
    val newPlatPlc: String? = null,
    val bun: String? = null,
    val ji: String? = null,
    val housGbnCd: String? = null,
    val housGbnCdNm: String? = null,
    val housTypCd: String? = null,
    val housTypCdNm: String? = null,
    val hoCnt: String? = null,
    val hhldCnt: String? = null,
    val totArea: String? = null,
    val useAprDay: String? = null,
    val crtnDay: String? = null,
)

/**
 * 공통 헤더
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class XmlHeader(
    val resultCode: String? = null,
    val resultMsg: String? = null,
)

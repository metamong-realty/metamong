package com.metamong.external.publicdata

import com.metamong.external.publicdata.dto.ParseResult
import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import org.springframework.stereotype.Component

/**
 * 공공데이터 API 응답 파서
 */
@Component
class PublicDataParser(
    private val jsonParser: PublicDataJsonParser,
) {
    /**
     * 아파트 매매 실거래가 파싱 (JSON)
     */
    fun parseApartmentTrade(
        response: String,
        lawdCd: String,
    ): ParseResult<ApartmentTradeRawDocumentEntity> = jsonParser.parseApartmentTrade(response, lawdCd)

    /**
     * 아파트 전월세 실거래가 파싱 (JSON)
     */
    fun parseApartmentRent(
        response: String,
        lawdCd: String,
    ): ParseResult<ApartmentRentRawDocumentEntity> = jsonParser.parseApartmentRent(response, lawdCd)

    /**
     * 공동주택 단지 목록 파싱 (JSON)
     */
    fun parseApartmentComplexList(response: String): ParseResult<ApartmentComplexListRawDocumentEntity> =
        jsonParser.parseApartmentComplexList(response)

    /**
     * 공동주택 기본 정보 파싱 (JSON)
     */
    fun parseApartmentComplexInfo(
        response: String,
        kaptCode: String,
    ): ApartmentComplexInfoRawDocumentEntity? = jsonParser.parseApartmentComplexInfo(response, kaptCode)

    /**
     * 주택인허가 정보 파싱 (JSON)
     */
    fun parseHousingLicense(
        response: String,
        sigunguCd: String,
        bjdongCd: String,
    ): ParseResult<HousingLicenseRawDocumentEntity> = jsonParser.parseHousingLicense(response, sigunguCd, bjdongCd)
}

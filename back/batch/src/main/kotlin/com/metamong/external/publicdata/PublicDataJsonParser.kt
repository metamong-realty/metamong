package com.metamong.external.publicdata

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.cfg.CoercionAction
import com.fasterxml.jackson.databind.cfg.CoercionInputShape
import com.fasterxml.jackson.databind.type.LogicalType
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.metamong.external.publicdata.dto.ApartmentComplexInfoJsonItem
import com.metamong.external.publicdata.dto.ApartmentComplexListJsonItem
import com.metamong.external.publicdata.dto.ApartmentRentJsonItem
import com.metamong.external.publicdata.dto.ApartmentTradeJsonItem
import com.metamong.external.publicdata.dto.HousingLicenseJsonResponse
import com.metamong.external.publicdata.dto.ParseResult
import com.metamong.external.publicdata.dto.PublicDataJsonListResponse
import com.metamong.external.publicdata.dto.PublicDataJsonResponse
import com.metamong.external.publicdata.dto.PublicDataJsonSingleResponse
import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PublicDataJsonParser {
    private val objectMapper: ObjectMapper =
        ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .apply {
                coercionConfigFor(LogicalType.POJO)
                    .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull)
            }.registerKotlinModule()

    /**
     * 아파트 매매 실거래가 JSON 파싱
     */
    fun parseApartmentTrade(
        json: String,
        lawdCd: String,
    ): ParseResult<ApartmentTradeRawDocumentEntity> =
        runCatching {
            val response =
                objectMapper.readValue(
                    json,
                    object : TypeReference<PublicDataJsonResponse<ApartmentTradeJsonItem>>() {},
                )
            val body = response.response?.body
            val items =
                body?.items?.item?.map { item ->
                    val compositeKey =
                        "$lawdCd-${item.dealYear.orEmpty()}-${item.dealMonth.orEmpty()}-${item.dealDay}-${item.aptSeq}-" +
                            "${item.floor}-${item.excluUseAr}-${item.dealAmount?.trim()}"
                    ApartmentTradeRawDocumentEntity(
                        compositeKey = compositeKey,
                        lawdCd = lawdCd,
                        dealYear = item.dealYear.orEmpty(),
                        dealMonth = item.dealMonth.orEmpty(),
                        dealDay = item.dealDay,
                        aptNm = item.aptNm,
                        aptSeq = item.aptSeq,
                        excluUseAr = item.excluUseAr,
                        dealAmount = item.dealAmount?.trim(),
                        floor = item.floor,
                        buildYear = item.buildYear,
                        roadNm = item.roadNm,
                        roadNmBonbun = item.roadNmBonbun,
                        roadNmBubun = item.roadNmBubun,
                        umdNm = item.umdNm,
                        jibun = item.jibun,
                        cdealType = item.cdealType,
                        cdealDay = item.cdealDay,
                        dealingGbn = item.dealingGbn,
                        rgstDate = item.rgstDate,
                        buyerGbn = item.buyerGbn,
                        slerGbn = item.slerGbn,
                        estateAgentSggNm = item.estateAgentSggNm,
                        collectedAt = LocalDateTime.now(),
                    )
                } ?: emptyList()

            ParseResult(
                items = items,
                totalCount = body?.totalCount ?: 0,
                pageNo = body?.pageNo ?: 1,
                numOfRows = body?.numOfRows ?: 0,
            )
        }.onFailure { e ->
            logger.error(e) { "아파트 매매 실거래가 JSON 파싱 실패 - lawdCd: $lawdCd" }
        }.getOrElse { ParseResult(emptyList(), 0, 1, 0) }

    /**
     * 아파트 전월세 실거래가 JSON 파싱
     */
    fun parseApartmentRent(
        json: String,
        lawdCd: String,
    ): ParseResult<ApartmentRentRawDocumentEntity> =
        runCatching {
            val response =
                objectMapper.readValue(
                    json,
                    object : TypeReference<PublicDataJsonResponse<ApartmentRentJsonItem>>() {},
                )
            val body = response.response?.body
            val items =
                body?.items?.item?.map { item ->
                    val compositeKey =
                        "$lawdCd-${item.dealYear.orEmpty()}-${item.dealMonth.orEmpty()}-${item.dealDay}-${item.aptSeq}-${item.floor}-${item.excluUseAr}-${item.deposit?.trim()}-${item.monthlyRent?.trim()}"
                    ApartmentRentRawDocumentEntity(
                        compositeKey = compositeKey,
                        lawdCd = lawdCd,
                        dealYear = item.dealYear.orEmpty(),
                        dealMonth = item.dealMonth.orEmpty(),
                        dealDay = item.dealDay,
                        aptNm = item.aptNm,
                        aptSeq = item.aptSeq,
                        excluUseAr = item.excluUseAr,
                        deposit = item.deposit?.trim(),
                        monthlyRent = item.monthlyRent?.trim(),
                        floor = item.floor,
                        buildYear = item.buildYear,
                        roadnm = item.roadnm,
                        umdNm = item.umdNm,
                        jibun = item.jibun,
                        sggCd = item.sggCd,
                        contractType = item.contractType,
                        contractTerm = item.contractTerm,
                        preDeposit = item.preDeposit,
                        preMonthlyRent = item.preMonthlyRent,
                        useRRRight = item.useRRRight,
                        collectedAt = LocalDateTime.now(),
                    )
                } ?: emptyList()

            ParseResult(
                items = items,
                totalCount = body?.totalCount ?: 0,
                pageNo = body?.pageNo ?: 1,
                numOfRows = body?.numOfRows ?: 0,
            )
        }.onFailure { e ->
            logger.error(e) { "아파트 전월세 실거래가 JSON 파싱 실패 - lawdCd: $lawdCd" }
        }.getOrElse { ParseResult(emptyList(), 0, 1, 0) }

    /**
     * 공동주택 단지 목록 JSON 파싱
     * API 응답: items가 배열로 직접 옴 (items: [...])
     */
    fun parseApartmentComplexList(json: String): ParseResult<ApartmentComplexListRawDocumentEntity> =
        runCatching {
            val response =
                objectMapper.readValue(
                    json,
                    object : TypeReference<PublicDataJsonListResponse<ApartmentComplexListJsonItem>>() {},
                )
            val body = response.response?.body
            val items =
                body?.items?.mapNotNull { item ->
                    item.kaptCode?.let { code ->
                        ApartmentComplexListRawDocumentEntity(
                            kaptCode = code,
                            kaptName = item.kaptName,
                            bjdCode = item.bjdCode,
                            as1 = item.as1,
                            as2 = item.as2,
                            as3 = item.as3,
                            as4 = item.as4,
                            collectedAt = LocalDateTime.now(),
                        )
                    }
                } ?: emptyList()

            ParseResult(
                items = items,
                totalCount = body?.totalCount ?: 0,
                pageNo = body?.pageNo ?: 1,
                numOfRows = body?.numOfRows ?: 0,
            )
        }.onFailure { e ->
            logger.error(e) { "공동주택 단지 목록 JSON 파싱 실패" }
        }.getOrElse { ParseResult(emptyList(), 0, 1, 0) }

    /**
     * 공동주택 기본 정보 JSON 파싱 (단일 아이템 응답)
     */
    fun parseApartmentComplexInfo(
        json: String,
        kaptCode: String,
    ): ApartmentComplexInfoRawDocumentEntity? =
        runCatching {
            val response =
                objectMapper.readValue(
                    json,
                    object : TypeReference<PublicDataJsonSingleResponse<ApartmentComplexInfoJsonItem>>() {},
                )
            response.response?.body?.item?.let { item ->
                ApartmentComplexInfoRawDocumentEntity(
                    kaptCode = kaptCode,
                    kaptName = item.kaptName,
                    kaptAddr = item.kaptAddr,
                    doroJuso = item.doroJuso,
                    codeSaleNm = item.codeSaleNm,
                    codeHeatNm = item.codeHeatNm,
                    codeHallNm = item.codeHallNm,
                    codeMgrNm = item.codeMgrNm,
                    codeAptNm = item.codeAptNm,
                    kaptTarea = item.kaptTarea,
                    kaptMarea = item.kaptMarea,
                    kaptDongCnt = item.kaptDongCnt,
                    hoCnt = item.hoCnt,
                    kaptdaCnt = item.kaptdaCnt,
                    kaptTopFloor = item.kaptTopFloor,
                    kaptBaseFloor = item.kaptBaseFloor,
                    ktownFlrNo = item.ktownFlrNo,
                    kaptMparea60 = item.kaptMparea60,
                    kaptMparea85 = item.kaptMparea85,
                    kaptMparea135 = item.kaptMparea135,
                    kaptMparea136 = item.kaptMparea136,
                    privArea = item.privArea,
                    kaptBcompany = item.kaptBcompany,
                    kaptAcompany = item.kaptAcompany,
                    kaptTel = item.kaptTel,
                    kaptFax = item.kaptFax,
                    kaptUrl = item.kaptUrl,
                    kaptUsedate = item.kaptUsedate,
                    bjdCode = item.bjdCode,
                    kaptdEcntp = item.kaptdEcntp,
                    zipcode = item.zipcode,
                    collectedAt = LocalDateTime.now(),
                )
            }
        }.onFailure { e ->
            logger.error(e) { "공동주택 기본 정보 JSON 파싱 실패 - kaptCode: $kaptCode" }
        }.getOrNull()

    /**
     * 주택인허가 정보 JSON 파싱
     * 주의: 이 API는 response 래퍼 없이 바로 header/body가 옴
     */
    fun parseHousingLicense(
        response: String,
        sigunguCd: String,
        bjdongCd: String,
    ): ParseResult<HousingLicenseRawDocumentEntity> =
        runCatching {
            val parsed =
                objectMapper.readValue(
                    response,
                    HousingLicenseJsonResponse::class.java,
                )
            val body = parsed.body
            val items =
                body?.items?.item?.map { item ->
                    HousingLicenseRawDocumentEntity(
                        mgmDongOulnPk = item.mgmDongOulnPk,
                        mgmHsrgstPk = item.mgmHsrgstPk,
                        sigunguCd = sigunguCd,
                        bjdongCd = bjdongCd,
                        platGbCd = item.platGbCd,
                        bun = item.bun,
                        ji = item.ji,
                        bldNm = item.bldNm,
                        splotNm = item.splotNm,
                        block = item.block,
                        lot = item.lot,
                        mainAtchGbCd = item.mainAtchGbCd,
                        mainAtchGbCdNm = item.mainAtchGbCdNm,
                        dongNm = item.dongNm,
                        mainPurpsCd = item.mainPurpsCd,
                        mainPurpsCdNm = item.mainPurpsCdNm,
                        hhldCntPeplRent = item.hhldCntPeplRent,
                        hhldCntPubRent_5 = item.hhldCntPubRent_5,
                        hhldCntPubRent_10 = item.hhldCntPubRent_10,
                        hhldCntPubRentEtc = item.hhldCntPubRentEtc,
                        hhldCntPubRentTot = item.hhldCntPubRentTot,
                        hhldCntPubLotou = item.hhldCntPubLotou,
                        hhldCntEmplRent = item.hhldCntEmplRent,
                        hhldCntLaborWlfar = item.hhldCntLaborWlfar,
                        hhldCntCvlRent = item.hhldCntCvlRent,
                        hhldCntCvlLotou = item.hhldCntCvlLotou,
                        strctCd = item.strctCd,
                        strctCdNm = item.strctCdNm,
                        roofCd = item.roofCd,
                        roofCdNm = item.roofCdNm,
                        archArea = item.archArea,
                        totArea = item.totArea,
                        ugrndArea = item.ugrndArea,
                        vlRatEstmTotArea = item.vlRatEstmTotArea,
                        ugrndFlrCnt = item.ugrndFlrCnt,
                        grndFlrCnt = item.grndFlrCnt,
                        heit = item.heit,
                        rideUseElvtCnt = item.rideUseElvtCnt,
                        emgenUseElvtCnt = item.emgenUseElvtCnt,
                        flrhFrom = item.flrhFrom,
                        ceilHeit = item.ceilHeit,
                        stairValidWidth = item.stairValidWidth,
                        hwayWidth = item.hwayWidth,
                        ouwlThick = item.ouwlThick,
                        adjHhldWallThick = item.adjHhldWallThick,
                        platPlc = item.platPlc,
                        crtnDay = item.crtnDay,
                        collectedAt = LocalDateTime.now(),
                    )
                } ?: emptyList()

            ParseResult(
                items = items,
                totalCount = body?.totalCount ?: 0,
                pageNo = body?.pageNo ?: 1,
                numOfRows = body?.numOfRows ?: 0,
            )
        }.onFailure { e ->
            logger.error(e) { "주택인허가 정보 JSON 파싱 실패 - sigunguCd: $sigunguCd, bjdongCd: $bjdongCd" }
        }.getOrElse { ParseResult(emptyList(), 0, 1, 0) }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

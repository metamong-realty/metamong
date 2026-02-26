package com.metamong.model.document.publicdata

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("ApartmentTradeRaw")
@CompoundIndexes(
    CompoundIndex(name = "idx_lawdCd_dealYearMonth", def = "{'lawdCd': 1, 'dealYear': 1, 'dealMonth': 1}"),
    CompoundIndex(name = "idx_compositeKey", def = "{'compositeKey': 1}", unique = true),
    CompoundIndex(name = "idx_dealYearMonth_id", def = "{'dealYear': 1, 'dealMonth': 1, '_id': 1}"),
    CompoundIndex(name = "idx_aptSeq_excluUseAr", def = "{'aptSeq': 1, 'excluUseAr': 1}"),
)
data class ApartmentTradeRawDocumentEntity(
    @Id val id: String? = null,
    val compositeKey: String,
    val lawdCd: String,
    val dealYear: String,
    val dealMonth: String,
    val dealDay: String?,
    val aptNm: String?,
    val aptSeq: String?,
    val excluUseAr: String?,
    val dealAmount: String?,
    val floor: String?,
    val buildYear: String?,
    val roadNm: String?,
    val roadNmBonbun: String?,
    val roadNmBubun: String?,
    val umdNm: String?,
    val jibun: String?,
    val cdealType: String?,
    val cdealDay: String?,
    val dealingGbn: String?,
    val rgstDate: String?,
    val buyerGbn: String?,
    val slerGbn: String?,
    val estateAgentSggNm: String?,
    val collectedAt: LocalDateTime = LocalDateTime.now(),
)

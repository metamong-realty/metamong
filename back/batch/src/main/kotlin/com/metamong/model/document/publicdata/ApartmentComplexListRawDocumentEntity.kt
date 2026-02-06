package com.metamong.model.document.publicdata

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 공동주택 단지 목록 Raw Document
 * API: 국토교통부 공동주택단지 목록제공 서비스
 */
@Document("ApartmentComplexListRaw")
@CompoundIndexes(
    CompoundIndex(name = "idx_as1_as2", def = "{'as1': 1, 'as2': 1}"),
)
data class ApartmentComplexListRawDocumentEntity(
    @Id val id: String? = null,
    @Indexed(unique = true)
    val kaptCode: String, // 단지코드 (예: "A10021295")
    val kaptName: String?, // 단지명 (예: "경희궁의아침4단지")
    val bjdCode: String?, // 법정동코드 (예: "1111011800")
    val as1: String?, // 시도명 (예: "서울특별시")
    val as2: String?, // 시군구명 (예: "종로구")
    val as3: String?, // 읍면동명 (예: "내수동")
    val as4: String?, // 리명 (대부분 null)
    val collectedAt: LocalDateTime = LocalDateTime.now(),
)

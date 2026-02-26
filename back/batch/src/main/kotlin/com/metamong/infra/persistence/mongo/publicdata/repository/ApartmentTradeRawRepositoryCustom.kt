package com.metamong.infra.persistence.mongo.publicdata.repository

import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDateTime

interface ApartmentTradeRawRepositoryCustom {
    fun findByDealYearMonthRange(
        criteria: Criteria,
        pageable: Pageable,
    ): List<ApartmentTradeRawDocumentEntity>

    fun countByDealYearMonthRange(criteria: Criteria): Long

    fun findDistinctAptSeqAndExcluUseAr(): List<Pair<String, String>>

    fun findAllByCursor(
        lastId: String?,
        pageSize: Int,
    ): List<ApartmentTradeRawDocumentEntity>

    fun findByCursorAndCollectedAtGte(
        lastId: String?,
        collectedAt: LocalDateTime,
        pageSize: Int,
    ): List<ApartmentTradeRawDocumentEntity>

    fun findByCursorAndDealYearMonthRange(
        lastId: String?,
        criteria: Criteria,
        pageSize: Int,
    ): List<ApartmentTradeRawDocumentEntity>
}

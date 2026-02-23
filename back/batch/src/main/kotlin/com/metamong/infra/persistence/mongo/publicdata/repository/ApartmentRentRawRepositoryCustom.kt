package com.metamong.infra.persistence.mongo.publicdata.repository

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria

interface ApartmentRentRawRepositoryCustom {
    fun findByDealYearMonthRange(
        criteria: Criteria,
        pageable: Pageable,
    ): List<ApartmentRentRawDocumentEntity>

    fun countByDealYearMonthRange(criteria: Criteria): Long

    fun findDistinctAptSeqAndExcluUseAr(): List<Pair<String, String>>
}

package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria

interface ApartmentTradeRawRepositoryCustom {
    fun findByDealYearMonthRange(
        criteria: Criteria,
        pageable: Pageable,
    ): List<ApartmentTradeRawDocumentEntity>

    fun countByDealYearMonthRange(criteria: Criteria): Long
}

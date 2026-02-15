package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria

interface ApartmentRentRawRepositoryCustom {
    fun findByDealYearMonthRange(
        criteria: Criteria,
        pageable: Pageable,
    ): List<ApartmentRentRawDocumentEntity>

    fun countByDealYearMonthRange(criteria: Criteria): Long
}

package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class ApartmentTradeRawRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : ApartmentTradeRawRepositoryCustom {
    override fun findByDealYearMonthRange(
        criteria: Criteria,
        pageable: Pageable,
    ): List<ApartmentTradeRawDocumentEntity> =
        mongoTemplate.find(Query(criteria).with(pageable), ApartmentTradeRawDocumentEntity::class.java)

    override fun countByDealYearMonthRange(criteria: Criteria): Long =
        mongoTemplate.count(Query(criteria), ApartmentTradeRawDocumentEntity::class.java)
}

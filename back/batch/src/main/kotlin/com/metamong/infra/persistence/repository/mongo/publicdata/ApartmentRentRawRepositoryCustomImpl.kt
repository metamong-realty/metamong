package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class ApartmentRentRawRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : ApartmentRentRawRepositoryCustom {
    override fun findByDealYearMonthRange(
        criteria: Criteria,
        pageable: Pageable,
    ): List<ApartmentRentRawDocumentEntity> = mongoTemplate.find(Query(criteria).with(pageable), ApartmentRentRawDocumentEntity::class.java)

    override fun countByDealYearMonthRange(criteria: Criteria): Long =
        mongoTemplate.count(Query(criteria), ApartmentRentRawDocumentEntity::class.java)
}

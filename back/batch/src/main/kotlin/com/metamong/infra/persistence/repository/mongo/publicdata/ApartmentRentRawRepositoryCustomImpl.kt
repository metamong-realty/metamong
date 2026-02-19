package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.bson.Document
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
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

    override fun findDistinctAptSeqAndExcluUseAr(): List<Pair<String, String>> {
        val aggregation =
            Aggregation.newAggregation(
                Aggregation.match(
                    Criteria
                        .where("aptSeq")
                        .ne(null)
                        .and("excluUseAr")
                        .ne(null),
                ),
                Aggregation.group("aptSeq", "excluUseAr"),
            )

        return mongoTemplate
            .aggregate(aggregation, "ApartmentRentRaw", Document::class.java)
            .mappedResults
            .map { doc ->
                val aptSeq = doc.getString("aptSeq")
                val excluUseAr = doc.getString("excluUseAr")
                aptSeq to excluUseAr
            }
    }
}

package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import org.bson.Document
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
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
            .aggregate(aggregation, "ApartmentTradeRaw", Document::class.java)
            .mappedResults
            .map { doc ->
                val aptSeq = doc.getString("aptSeq")
                val excluUseAr = doc.getString("excluUseAr")
                aptSeq to excluUseAr
            }
    }
}

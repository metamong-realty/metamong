package com.metamong.infra.persistence.mongo.publicdata.repository

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

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

    override fun findAllByCursor(
        lastId: String?,
        pageSize: Int,
    ): List<ApartmentRentRawDocumentEntity> {
        val query = Query()
        if (lastId != null) {
            query.addCriteria(Criteria.where("_id").gt(ObjectId(lastId)))
        }
        query.with(Sort.by("_id")).limit(pageSize)
        return mongoTemplate.find(query, ApartmentRentRawDocumentEntity::class.java)
    }

    override fun findByCursorAndCollectedAtGte(
        lastId: String?,
        collectedAt: LocalDateTime,
        pageSize: Int,
    ): List<ApartmentRentRawDocumentEntity> {
        val query = Query(Criteria.where("collectedAt").gte(collectedAt))
        if (lastId != null) {
            query.addCriteria(Criteria.where("_id").gt(ObjectId(lastId)))
        }
        query.with(Sort.by("_id")).limit(pageSize)
        return mongoTemplate.find(query, ApartmentRentRawDocumentEntity::class.java)
    }

    override fun findByCursorAndDealYearMonthRange(
        lastId: String?,
        criteria: Criteria,
        pageSize: Int,
    ): List<ApartmentRentRawDocumentEntity> {
        val query = Query(criteria)
        if (lastId != null) {
            query.addCriteria(Criteria.where("_id").gt(ObjectId(lastId)))
        }
        query.with(Sort.by("_id")).limit(pageSize)
        return mongoTemplate.find(query, ApartmentRentRawDocumentEntity::class.java)
    }

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
            .mapNotNull { doc ->
                val id = doc.get("_id", Document::class.java) ?: return@mapNotNull null
                val aptSeq = id.getString("aptSeq") ?: return@mapNotNull null
                val excluUseAr = id.getString("excluUseAr") ?: return@mapNotNull null
                aptSeq to excluUseAr
            }
    }
}

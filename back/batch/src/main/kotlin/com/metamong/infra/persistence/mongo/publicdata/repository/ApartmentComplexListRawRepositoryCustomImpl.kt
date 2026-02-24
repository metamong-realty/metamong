package com.metamong.infra.persistence.mongo.publicdata.repository

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import kotlin.collections.get

@Repository
class ApartmentComplexListRawRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate,
) : ApartmentComplexListRawRepositoryCustom {
    override fun findAllKaptCodesOnly(): List<String> {
        val query = Query()
        query.fields().include("kaptCode").exclude("_id")

        return mongoTemplate
            .find(
                query,
                Map::class.java,
                "ApartmentComplexListRaw",
            ).mapNotNull { it["kaptCode"] as? String }
    }
}

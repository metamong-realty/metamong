package com.metamong.infra.persistence.mongo.publicdata.repository

import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface ApartmentComplexInfoRawRepository : MongoRepository<ApartmentComplexInfoRawDocumentEntity, String> {
    // Spring Data MongoDB의 method naming으로 대체
    // @Query("{ 'bjdCode': { $regex: ?0 } }") 와 동일한 기능
    fun findByBjdCodeStartingWith(sigunguCodePrefix: String): List<ApartmentComplexInfoRawDocumentEntity>
}

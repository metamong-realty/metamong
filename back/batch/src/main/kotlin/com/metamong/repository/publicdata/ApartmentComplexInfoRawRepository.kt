package com.metamong.repository.publicdata

import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ApartmentComplexInfoRawRepository : MongoRepository<ApartmentComplexInfoRawDocumentEntity, String> {
    @Query("{ 'bjdCode': { \$regex: ?0 } }")
    fun findByBjdCodeStartingWith(sigunguCodePrefix: String): List<ApartmentComplexInfoRawDocumentEntity>
}

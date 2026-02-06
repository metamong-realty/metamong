package com.metamong.repository.publicdata

import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ApartmentComplexListRawRepository : MongoRepository<ApartmentComplexListRawDocumentEntity, String> {
    @Query(value = "{}", fields = "{ 'kaptCode': 1 }")
    fun findAllKaptCodes(): List<ApartmentComplexListRawDocumentEntity>
}

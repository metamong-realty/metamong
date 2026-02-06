package com.metamong.infra.persistance.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface ApartmentComplexListRawRepository :
    MongoRepository<ApartmentComplexListRawDocumentEntity, String>,
    ApartmentComplexListRawRepositoryCustom {
    // @Query 제거 - Custom Repository로 대체
    // 기존: @Query(value = "{}", fields = "{ 'kaptCode': 1 }")
    // 대체: findAllKaptCodesOnly() in Custom Repository
}

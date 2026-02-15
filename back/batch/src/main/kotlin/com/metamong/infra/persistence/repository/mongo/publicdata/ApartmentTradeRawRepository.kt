package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface ApartmentTradeRawRepository :
    MongoRepository<ApartmentTradeRawDocumentEntity, String>,
    ApartmentTradeRawRepositoryCustom {
    fun findAllBy(pageable: Pageable): Page<ApartmentTradeRawDocumentEntity>

    fun findByCollectedAtGreaterThanEqual(
        collectedAt: LocalDateTime,
        pageable: Pageable,
    ): Page<ApartmentTradeRawDocumentEntity>

    fun countByCollectedAtGreaterThanEqual(collectedAt: LocalDateTime): Long
}

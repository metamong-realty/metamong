package com.metamong.infra.persistence.repository.mongo.publicdata

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface ApartmentRentRawRepository : MongoRepository<ApartmentRentRawDocumentEntity, String> {
    fun findAllBy(pageable: Pageable): Page<ApartmentRentRawDocumentEntity>

    fun findByCollectedAtGreaterThanEqual(
        collectedAt: LocalDateTime,
        pageable: Pageable,
    ): Page<ApartmentRentRawDocumentEntity>

    fun countByCollectedAtGreaterThanEqual(collectedAt: LocalDateTime): Long
}

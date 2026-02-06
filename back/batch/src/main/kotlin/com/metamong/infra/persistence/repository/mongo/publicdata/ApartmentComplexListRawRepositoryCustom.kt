package com.metamong.infra.persistence.repository.mongo.publicdata

interface ApartmentComplexListRawRepositoryCustom {
    fun findAllKaptCodesOnly(): List<String>
}

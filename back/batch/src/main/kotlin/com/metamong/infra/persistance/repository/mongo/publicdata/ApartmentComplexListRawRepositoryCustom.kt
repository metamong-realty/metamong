package com.metamong.infra.persistance.repository.mongo.publicdata

interface ApartmentComplexListRawRepositoryCustom {
    fun findAllKaptCodesOnly(): List<String>
}

package com.metamong.infra.persistence.mongo.publicdata.repository

interface ApartmentComplexListRawRepositoryCustom {
    fun findAllKaptCodesOnly(): List<String>
}

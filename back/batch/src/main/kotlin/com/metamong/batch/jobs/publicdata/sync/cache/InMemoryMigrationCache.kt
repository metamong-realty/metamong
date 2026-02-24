package com.metamong.batch.jobs.publicdata.sync.cache

import org.springframework.stereotype.Component

@Component
class InMemoryMigrationCache {
    @Volatile
    private var aptSeqToComplexId: Map<String, Long> = emptyMap()

    @Volatile
    private var unitTypeIdCache: Map<String, Long> = emptyMap()

    fun loadAptSeqMappings(mappings: Map<String, Long>) {
        aptSeqToComplexId = mappings
    }

    fun loadUnitTypes(entries: Map<String, Long>) {
        unitTypeIdCache = entries
    }

    fun getComplexId(aptSeq: String): Long? = aptSeqToComplexId[aptSeq]

    fun getUnitTypeId(
        complexId: Long,
        exclusivePyeong: Short,
    ): Long? = unitTypeIdCache["$complexId:$exclusivePyeong"]

    fun clear() {
        aptSeqToComplexId = emptyMap()
        unitTypeIdCache = emptyMap()
    }
}

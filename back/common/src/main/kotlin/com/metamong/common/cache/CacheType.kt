package com.metamong.common.cache

import java.time.Duration

// @Cacheable를 위한 캐시 키
sealed class CacheType(
    val cacheName: String,
    val ttl: Duration,
) {
    data object ContentKeywordPattern : CacheType(CONTENT_KEYWORD_PATTERN, Duration.ofDays(1))

    data object UnsearchableBoardIds : CacheType(UNSEARCHABLE_BOARD_IDS, Duration.ofHours(1))
    
    data object ApartmentSequenceToComplexId : CacheType(APARTMENT_SEQUENCE_TO_COMPLEX_ID, Duration.ofHours(24))

    data object UnitType : CacheType(UNIT_TYPE, Duration.ofHours(24))

    companion object {
        const val CONTENT_KEYWORD_PATTERN = "contentKeywordPattern"
        const val UNSEARCHABLE_BOARD_IDS = "unsearchableBoardIds"
        const val APARTMENT_SEQUENCE_TO_COMPLEX_ID = "apartmentSequenceToComplexId"
        const val UNIT_TYPE = "unitType"

        fun entries(): List<CacheType> = CacheType::class.sealedSubclasses.mapNotNull { it.objectInstance }
    }
}

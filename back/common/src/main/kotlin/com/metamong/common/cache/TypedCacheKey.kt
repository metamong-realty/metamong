package com.metamong.common.cache

sealed interface TypedCacheKey<in T> {
    val expiredMinute: Long
    val description: String

    fun generate(input: T): CacheKey
}

data class CacheKey(
    val key: String,
    val expiredMinute: Long,
)

data object CurationGroupsCacheKey : TypedCacheKey<String> {
    override val expiredMinute = 60L
    override val description = "큐레이션 그룹 목록"

    override fun generate(groupCode: String): CacheKey {
        val key = "cache:curation:group-code:$groupCode"
        return CacheKey(key, expiredMinute)
    }
}

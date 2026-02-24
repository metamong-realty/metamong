package com.metamong.infra.security

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TokenBlacklistService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun blacklist(
        token: String,
        remainingExpirationMs: Long,
    ) {
        if (remainingExpirationMs > 0) {
            val key = buildKey(token)
            redisTemplate.opsForValue().set(key, "true", remainingExpirationMs, TimeUnit.MILLISECONDS)
        }
    }

    fun isBlacklisted(token: String): Boolean {
        val key = buildKey(token)
        return redisTemplate.hasKey(key)
    }

    private fun buildKey(token: String): String = "$KEY_PREFIX$token"

    companion object {
        private const val KEY_PREFIX = "blacklist:"
    }
}

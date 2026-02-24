package com.metamong.infra.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RefreshTokenService(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long,
) {
    fun save(
        userId: Long,
        refreshToken: String,
    ) {
        val key = buildKey(userId)
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiry, TimeUnit.MILLISECONDS)
    }

    fun find(userId: Long): String? {
        val key = buildKey(userId)
        return redisTemplate.opsForValue().get(key)
    }

    fun delete(userId: Long) {
        val key = buildKey(userId)
        redisTemplate.delete(key)
    }

    private fun buildKey(userId: Long): String = "$KEY_PREFIX$userId"

    companion object {
        private const val KEY_PREFIX = "refresh_token:"
    }
}

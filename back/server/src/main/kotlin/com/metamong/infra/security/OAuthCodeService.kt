package com.metamong.infra.security

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class OAuthCodeService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun create(
        userId: Long,
        refreshToken: String,
        accessToken: String,
    ): String {
        val code = UUID.randomUUID().toString()
        // userId:refreshToken:accessToken 형태로 저장, 30초 TTL
        val value = "$userId:$refreshToken:$accessToken"
        redisTemplate.opsForValue().set(keyOf(code), value, 30, TimeUnit.SECONDS)
        return code
    }

    fun exchange(code: String): OAuthTokens? {
        val key = keyOf(code)
        val value = redisTemplate.opsForValue().get(key) ?: return null
        redisTemplate.delete(key) // 1회용
        val parts = value.split(":", limit = 3)
        if (parts.size != 3) return null
        return OAuthTokens(
            userId = parts[0].toLong(),
            refreshToken = parts[1],
            accessToken = parts[2],
        )
    }

    private fun keyOf(code: String) = "oauth_code:$code"

    data class OAuthTokens(
        val userId: Long,
        val refreshToken: String,
        val accessToken: String,
    )
}

package com.metamong.infra.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiry}") private val accessTokenExpiry: Long,
    @Value("\${jwt.refresh-token-expiry}") private val refreshTokenExpiry: Long,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun createAccessToken(
        userId: Long,
        email: String,
    ): String = createToken(userId, email, accessTokenExpiry)

    fun createRefreshToken(
        userId: Long,
        email: String,
    ): String = createToken(userId, email, refreshTokenExpiry)

    fun getUserId(token: String): Long = parseClaims(token).subject.toLong()

    fun getEmail(token: String): String = parseClaims(token)["email"] as String

    fun validate(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            when (e) {
                is SecurityException, is MalformedJwtException ->
                    logger.warn { "잘못된 JWT 서명입니다." }
                is ExpiredJwtException ->
                    logger.warn { "만료된 JWT 토큰입니다." }
                is UnsupportedJwtException ->
                    logger.warn { "지원되지 않는 JWT 토큰입니다." }
                is IllegalArgumentException ->
                    logger.warn { "JWT 토큰이 잘못되었습니다." }
            }
            false
        }

    fun getRemainingExpiration(token: String): Long {
        val expiration = parseClaims(token).expiration
        return expiration.time - System.currentTimeMillis()
    }

    fun getAccessTokenExpiry(): Long = accessTokenExpiry

    private fun createToken(
        userId: Long,
        email: String,
        expiry: Long,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)

        return Jwts
            .builder()
            .setSubject(userId.toString())
            .claim("email", email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun parseClaims(token: String): Claims =
        Jwts
            .parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

package com.metamong.application.auth.service

import com.metamong.application.auth.response.TokenResponse
import com.metamong.domain.auth.exception.AuthException
import com.metamong.infra.security.JwtTokenProvider
import com.metamong.infra.security.RefreshTokenService
import com.metamong.infra.security.TokenBlacklistService
import org.springframework.stereotype.Service

@Service
class AuthCommandService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val tokenBlacklistService: TokenBlacklistService,
) {
    fun refresh(refreshToken: String): TokenResponse {
        if (!jwtTokenProvider.validate(refreshToken)) {
            throw AuthException.TokenInvalid()
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val email = jwtTokenProvider.getEmail(refreshToken)

        val savedToken =
            refreshTokenService.find(userId)
                ?: throw AuthException.RefreshTokenNotFound()

        if (savedToken != refreshToken) {
            throw AuthException.RefreshTokenMismatch()
        }

        val newAccessToken = jwtTokenProvider.createAccessToken(userId, email)

        return TokenResponse(
            accessToken = newAccessToken,
            expiresIn = jwtTokenProvider.getAccessTokenExpiry(),
        )
    }

    fun logout(
        userId: Long,
        accessToken: String,
    ) {
        refreshTokenService.delete(userId)
        val remainingExpiration = jwtTokenProvider.getRemainingExpiration(accessToken)
        tokenBlacklistService.blacklist(accessToken, remainingExpiration)
    }
}

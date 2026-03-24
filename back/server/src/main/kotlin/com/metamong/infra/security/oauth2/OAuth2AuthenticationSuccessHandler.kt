package com.metamong.infra.security.oauth2

import com.metamong.infra.security.JwtTokenProvider
import com.metamong.infra.security.OAuthCodeService
import com.metamong.infra.security.RefreshTokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val oAuthCodeService: OAuthCodeService,
    @Value("\${app.oauth2.redirect-uri}") private val redirectUri: String,
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as CustomOAuth2User
        val user = oAuth2User.userEntity
        val userId = user.id
        val email = user.email

        val accessToken = jwtTokenProvider.createAccessToken(userId, email)
        val refreshToken = jwtTokenProvider.createRefreshToken(userId, email)

        refreshTokenService.save(userId, refreshToken)

        // 단기 code 발급 (30초 TTL, 1회용)
        // FE /oauth/callback → POST /api/v1/auth/exchange → BE Set-Cookie
        // API Route proxy를 통해 FE 도메인으로 httpOnly cookie set
        val code = oAuthCodeService.create(userId, refreshToken, accessToken)

        val targetUrl =
            UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("code", code)
                .build()
                .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}

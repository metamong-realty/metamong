package com.metamong.infra.security.oauth2

import com.metamong.infra.security.JwtTokenProvider
import com.metamong.infra.security.RefreshTokenService
import jakarta.servlet.http.Cookie
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
    @Value("\${app.oauth2.redirect-uri}") private val redirectUri: String,
    @Value("\${app.oauth2.cookie-secure:false}") private val cookieSecure: Boolean,
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

        // refresh token → httpOnly cookie (JS 접근 불가, XSS 안전)
        val cookie =
            Cookie("refreshToken", refreshToken).apply {
                isHttpOnly = true
                secure = cookieSecure
                path = "/"
                maxAge = 7 * 24 * 60 * 60 // 7일
            }
        response.addCookie(cookie)

        // access token만 URL param으로 전달 (FE에서 메모리에 저장)
        val targetUrl =
            UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}

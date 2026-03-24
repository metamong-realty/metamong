package com.metamong.application.auth.controller

import com.metamong.application.auth.response.TokenResponse
import com.metamong.application.auth.response.UserMeResponse
import com.metamong.application.auth.service.AuthCommandService
import com.metamong.application.auth.service.AuthQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.security.CurrentUser
import com.metamong.infra.security.OAuthCodeService
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authCommandService: AuthCommandService,
    private val authQueryService: AuthQueryService,
    private val oAuthCodeService: OAuthCodeService,
) {
    // OAuth 로그인 후 단기 code를 토큰으로 교환
    // FE가 API Route proxy를 통해 호출 → Set-Cookie가 FE 도메인으로 set
    @PostMapping("/exchange")
    fun exchange(
        @RequestBody body: Map<String, String>,
        response: HttpServletResponse,
    ): ApiResponse<TokenResponse> {
        val code = body["code"] ?: throw IllegalArgumentException("code is required")
        val tokens =
            oAuthCodeService.exchange(code)
                ?: throw IllegalArgumentException("Invalid or expired code")

        setCookieHeader(response, tokens.refreshToken)

        return ApiResponse.ok(
            TokenResponse(
                accessToken = tokens.accessToken,
                expiresIn = 3_600_000,
            ),
        )
    }

    // refresh token cookie로 access token 갱신
    @PostMapping("/refresh")
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<TokenResponse> {
        val refreshToken =
            request.cookies
                ?.firstOrNull { it.name == "refreshToken" }
                ?.value
                ?: throw IllegalArgumentException("Refresh token cookie not found")

        val result = authCommandService.refresh(refreshToken)

        val newToken = result.refreshToken ?: refreshToken
        setCookieHeader(response, newToken)

        return ApiResponse.ok(result)
    }

    @PostMapping("/logout")
    fun logout(
        @CurrentUser userId: Long,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ApiResponse<Unit> {
        val accessToken = request.getHeader("Authorization")?.substringAfter("Bearer ") ?: ""
        authCommandService.logout(userId, accessToken)
        clearCookieHeader(response)
        return ApiResponse.ok(Unit)
    }

    @GetMapping("/me")
    fun me(
        @CurrentUser userId: Long,
    ): ApiResponse<UserMeResponse> {
        val result = authQueryService.getMe(userId)
        return ApiResponse.ok(result)
    }

    private fun setCookieHeader(
        response: HttpServletResponse,
        refreshToken: String,
    ) {
        val maxAge = 7 * 24 * 60 * 60
        // SameSite=Lax: FE/BE 동일 도메인(proxy) 사용으로 충분
        response.addHeader(
            "Set-Cookie",
            "refreshToken=$refreshToken; HttpOnly; Path=/; Max-Age=$maxAge; SameSite=Lax",
        )
    }

    private fun clearCookieHeader(response: HttpServletResponse) {
        response.addHeader(
            "Set-Cookie",
            "refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax",
        )
    }
}

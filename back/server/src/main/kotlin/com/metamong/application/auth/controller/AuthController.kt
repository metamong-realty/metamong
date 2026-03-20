package com.metamong.application.auth.controller

import com.metamong.application.auth.response.TokenResponse
import com.metamong.application.auth.response.UserMeResponse
import com.metamong.application.auth.service.AuthCommandService
import com.metamong.application.auth.service.AuthQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.security.CurrentUser
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authCommandService: AuthCommandService,
    private val authQueryService: AuthQueryService,
    @Value("\${app.oauth2.cookie-secure:false}") private val cookieSecure: Boolean,
) {
    // refresh token은 httpOnly cookie에서 읽음
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

        // 새 refresh token cookie 갱신 (SameSite=None for cross-site)
        val maxAge = 7 * 24 * 60 * 60
        val newToken = result.refreshToken ?: refreshToken
        val cookieHeader =
            buildString {
                append("refreshToken=$newToken")
                append("; HttpOnly")
                append("; Path=/")
                append("; Max-Age=$maxAge")
                if (cookieSecure) {
                    append("; Secure")
                    append("; SameSite=None")
                } else {
                    append("; SameSite=Lax")
                }
            }
        response.addHeader("Set-Cookie", cookieHeader)

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

        // refresh token cookie 삭제
        val deleteCookie =
            buildString {
                append("refreshToken=")
                append("; HttpOnly")
                append("; Path=/")
                append("; Max-Age=0")
                if (cookieSecure) {
                    append("; Secure")
                    append("; SameSite=None")
                } else {
                    append("; SameSite=Lax")
                }
            }
        response.addHeader("Set-Cookie", deleteCookie)

        return ApiResponse.ok(Unit)
    }

    @GetMapping("/me")
    fun me(
        @CurrentUser userId: Long,
    ): ApiResponse<UserMeResponse> {
        val result = authQueryService.getMe(userId)
        return ApiResponse.ok(result)
    }
}

package com.metamong.application.auth.controller

import com.metamong.application.auth.request.TokenRefreshRequest
import com.metamong.application.auth.response.TokenResponse
import com.metamong.application.auth.response.UserMeResponse
import com.metamong.application.auth.service.AuthCommandService
import com.metamong.application.auth.service.AuthQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.security.CurrentUser
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
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
) {
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: TokenRefreshRequest,
    ): ApiResponse<TokenResponse> {
        val result = authCommandService.refresh(request.refreshToken)
        return ApiResponse.ok(result)
    }

    @PostMapping("/logout")
    fun logout(
        @CurrentUser userId: Long,
        request: HttpServletRequest,
    ): ApiResponse<Unit> {
        val accessToken = request.getHeader("Authorization")?.substringAfter("Bearer ") ?: ""
        authCommandService.logout(userId, accessToken)
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

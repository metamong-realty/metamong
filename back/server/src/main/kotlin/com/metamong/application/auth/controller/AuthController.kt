package com.metamong.application.auth.controller

import com.metamong.application.auth.request.TokenRefreshRequest
import com.metamong.application.auth.response.TokenResponse
import com.metamong.application.auth.response.UserMeResponse
import com.metamong.application.auth.service.AuthCommandService
import com.metamong.application.auth.service.AuthQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "인증 API", description = "토큰 갱신, 로그아웃, 내 정보 조회 API")
class AuthController(
    private val authCommandService: AuthCommandService,
    private val authQueryService: AuthQueryService,
) {
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: TokenRefreshRequest,
    ): ApiResponse<TokenResponse> {
        val result = authCommandService.refresh(request.refreshToken)
        return ApiResponse.ok(result)
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 Access Token을 블랙리스트에 등록합니다.")
    @PostMapping("/logout")
    fun logout(
        @CurrentUser userId: Long,
        request: HttpServletRequest,
    ): ApiResponse<Unit> {
        val accessToken = request.getHeader("Authorization")?.substringAfter("Bearer ") ?: ""
        authCommandService.logout(userId, accessToken)
        return ApiResponse.ok(Unit)
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    fun me(
        @CurrentUser userId: Long,
    ): ApiResponse<UserMeResponse> {
        val result = authQueryService.getMe(userId)
        return ApiResponse.ok(result)
    }
}

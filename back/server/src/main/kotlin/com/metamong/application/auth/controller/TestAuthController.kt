package com.metamong.application.auth.controller

import com.metamong.application.auth.service.AuthQueryService
import com.metamong.common.response.ApiResponse
import com.metamong.infra.persistence.user.repository.UserRepository
import com.metamong.infra.security.JwtTokenProvider
import com.metamong.infra.security.OAuthCodeService
import com.metamong.infra.security.RefreshTokenService
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 로컬/개발 환경 전용 테스트 로그인 엔드포인트
 * - prod 프로파일에서는 빈이 등록되지 않음
 * - E2E 자동화 테스트 목적
 */
@Hidden
@Profile("local", "test", "dev")
@RestController
@RequestMapping("/v1/auth/test")
class TestAuthController(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenService: RefreshTokenService,
    private val oAuthCodeService: OAuthCodeService,
    private val authQueryService: AuthQueryService,
) {
    data class TestLoginRequest(
        val userId: Long,
    )

    data class TestLoginResponse(
        val code: String,
        val userId: Long,
        val email: String,
    )

    /**
     * userId로 바로 OAuth code 발급
     * POST /v1/auth/test/login { "userId": 5 }
     * → code 반환 → /api/v1/auth/exchange 호출로 cookie 세팅
     */
    @PostMapping("/login")
    fun testLogin(
        @RequestBody request: TestLoginRequest,
    ): ApiResponse<TestLoginResponse> {
        val user =
            userRepository.findById(request.userId).orElseThrow {
                IllegalArgumentException("User not found: ${request.userId}")
            }

        val accessToken = jwtTokenProvider.createAccessToken(user.id, user.email)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)
        refreshTokenService.save(user.id, refreshToken)

        val code = oAuthCodeService.create(user.id, refreshToken, accessToken)

        return ApiResponse.ok(
            TestLoginResponse(
                code = code,
                userId = user.id,
                email = user.email,
            ),
        )
    }

    /**
     * 현재 DB의 첫 번째 유저로 빠르게 로그인 (userId 없을 때 편의용)
     */
    @PostMapping("/login/first-user")
    fun testLoginFirstUser(response: HttpServletResponse): ApiResponse<TestLoginResponse> {
        val user =
            userRepository.findAll().firstOrNull()
                ?: throw IllegalStateException("No users in DB")

        val accessToken = jwtTokenProvider.createAccessToken(user.id, user.email)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email)
        refreshTokenService.save(user.id, refreshToken)

        val code = oAuthCodeService.create(user.id, refreshToken, accessToken)

        return ApiResponse.ok(
            TestLoginResponse(
                code = code,
                userId = user.id,
                email = user.email,
            ),
        )
    }
}

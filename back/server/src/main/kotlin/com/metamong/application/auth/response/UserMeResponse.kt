package com.metamong.application.auth.response

import com.metamong.domain.user.model.SocialProvider
import com.metamong.domain.user.model.UserEntity
import com.metamong.domain.user.model.UserStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class UserMeResponse(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,
    @Schema(description = "사용자 상태", example = "ACTIVE")
    val status: UserStatus,
    @Schema(description = "연동된 소셜 로그인 제공자 목록")
    val linkedProviders: List<SocialProvider>,
    @Schema(description = "가입일시")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(user: UserEntity): UserMeResponse {
            val providers = mutableListOf<SocialProvider>()
            if (user.kakaoId != null) providers.add(SocialProvider.KAKAO)
            if (user.naverId != null) providers.add(SocialProvider.NAVER)
            if (user.googleId != null) providers.add(SocialProvider.GOOGLE)

            return UserMeResponse(
                id = user.id,
                email = user.email,
                nickname = user.nickname,
                status = user.status,
                linkedProviders = providers,
                createdAt = user.createdAt,
            )
        }
    }
}

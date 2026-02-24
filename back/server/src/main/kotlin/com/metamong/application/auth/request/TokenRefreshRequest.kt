package com.metamong.application.auth.request

import jakarta.validation.constraints.NotBlank

data class TokenRefreshRequest(
    @field:NotBlank(message = "Refresh Token은 필수입니다")
    val refreshToken: String,
)

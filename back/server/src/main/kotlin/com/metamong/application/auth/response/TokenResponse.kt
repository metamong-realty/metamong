package com.metamong.application.auth.response

import io.swagger.v3.oas.annotations.media.Schema

data class TokenResponse(
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,
    @Schema(description = "Refresh Token (갱신 시 미포함)", example = "eyJhbGciOiJIUzI1NiJ9...")
    val refreshToken: String? = null,
    @Schema(description = "Access Token 만료 시간 (ms)", example = "3600000")
    val expiresIn: Long,
)

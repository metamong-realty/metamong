package com.metamong.domain.auth.exception

import com.metamong.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class AuthException(
    status: HttpStatus,
    message: String,
    code: String? = null,
) : CustomException(status, message, code) {
    class TokenExpired(
        message: String = "토큰이 만료되었습니다.",
    ) : AuthException(HttpStatus.UNAUTHORIZED, message, "AUTH_001")

    class TokenInvalid(
        message: String = "유효하지 않은 토큰입니다.",
    ) : AuthException(HttpStatus.UNAUTHORIZED, message, "AUTH_002")

    class RefreshTokenNotFound(
        message: String = "Refresh Token을 찾을 수 없습니다.",
    ) : AuthException(HttpStatus.UNAUTHORIZED, message, "AUTH_003")

    class RefreshTokenMismatch(
        message: String = "Refresh Token이 일치하지 않습니다.",
    ) : AuthException(HttpStatus.UNAUTHORIZED, message, "AUTH_004")

    class Unauthorized(
        message: String = "인증이 필요합니다.",
    ) : AuthException(HttpStatus.UNAUTHORIZED, message, "AUTH_005")
}

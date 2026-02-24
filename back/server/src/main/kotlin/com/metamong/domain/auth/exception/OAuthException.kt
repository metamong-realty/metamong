package com.metamong.domain.auth.exception

import com.metamong.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class OAuthException(
    status: HttpStatus,
    message: String,
    code: String? = null,
) : CustomException(status, message, code) {
    class UnsupportedProvider(
        message: String = "지원하지 않는 소셜 로그인입니다.",
    ) : OAuthException(HttpStatus.BAD_REQUEST, message, "OAUTH_001")
}

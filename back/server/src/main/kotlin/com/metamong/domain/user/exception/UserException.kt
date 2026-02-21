package com.metamong.domain.user.exception

import com.metamong.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class UserException(
    status: HttpStatus,
    message: String,
    code: String? = null,
) : CustomException(status, message, code) {
    class NotFound(
        message: String = "사용자를 찾을 수 없습니다.",
    ) : UserException(HttpStatus.NOT_FOUND, message, "USER_001")

    class AlreadyWithdrawn(
        message: String = "이미 탈퇴한 사용자입니다.",
    ) : UserException(HttpStatus.BAD_REQUEST, message, "USER_002")

    class EmailRequired(
        message: String = "이메일 정보가 필요합니다.",
    ) : UserException(HttpStatus.BAD_REQUEST, message, "USER_003")
}

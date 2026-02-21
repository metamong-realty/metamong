package com.metamong.common.exception

import org.springframework.http.HttpStatus

sealed class CommonException(
    status: HttpStatus,
    message: String,
    code: String? = null,
) : CustomException(status, message, code) {
    class BadRequest(
        message: String = "잘못된 요청입니다.",
    ) : CommonException(HttpStatus.BAD_REQUEST, message, "REQUEST_001")

    class InvalidParameter(
        message: String = "파라미터 값이 올바르지 않습니다.",
    ) : CommonException(HttpStatus.BAD_REQUEST, message, "REQUEST_003")

    class ParameterRequired(
        message: String = "필수 파라미터가 누락되었습니다.",
    ) : CommonException(HttpStatus.BAD_REQUEST, message, "REQUEST_002")

    class ResourceNotFound(
        message: String = "요청한 리소스를 찾을 수 없습니다.",
    ) : CommonException(HttpStatus.NOT_FOUND, message, "RESOURCE_001")

    class InternalServerError(
        message: String = "서버 내부 오류가 발생했습니다.",
    ) : CommonException(HttpStatus.INTERNAL_SERVER_ERROR, message, "SERVER_001")
}

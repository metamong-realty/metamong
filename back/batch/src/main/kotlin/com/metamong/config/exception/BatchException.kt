package com.metamong.config.exception

import com.metamong.common.exception.CustomException
import org.springframework.http.HttpStatus

sealed class BatchException(
    status: HttpStatus,
    message: String,
    code: String? = null,
) : CustomException(status, message, code) {
    class JobNotFound(
        message: String = "배치 작업을 찾을 수 없습니다.",
    ) : BatchException(HttpStatus.BAD_REQUEST, message, "BATCH_001")

    class JobExecutionFailed(
        message: String = "배치 작업 실행에 실패했습니다.",
    ) : BatchException(HttpStatus.INTERNAL_SERVER_ERROR, message, "BATCH_002")

    class ParameterInvalid(
        message: String = "배치 작업 파라미터가 올바르지 않습니다.",
    ) : BatchException(HttpStatus.BAD_REQUEST, message, "BATCH_003")

    class ExternalApiError(
        message: String = "외부 API 호출 중 오류가 발생했습니다.",
    ) : BatchException(HttpStatus.INTERNAL_SERVER_ERROR, message, "SERVER_002")
}

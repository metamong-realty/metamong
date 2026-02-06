package com.metamong.config.exception

enum class ErrorCodes(
    val code: String,
    val message: String,
) {
    /**
     * 코드정의
     * GLOBAL_XXX : 전역적인 코드
     * BATCH_XXX : 배치 작업 관련
     */

    GLOBAL_ERROR("GLOBAL_001", "요청 처리 중 오류가 발생했습니다. 페이지를 새로 고치거나 잠시 후 다시 시도해 주세요."),

    BATCH_JOB_NOT_FOUND("BATCH_001", "배치 작업을 찾을 수 없습니다."),
    BATCH_JOB_EXECUTION_FAILED("BATCH_002", "배치 작업 실행에 실패했습니다."),
    BATCH_PARAMETER_INVALID("BATCH_003", "배치 작업 파라미터가 올바르지 않습니다."),

    /**
     * 400 Bad Request
     */
    INVALID_REQUEST("REQUEST_001", "잘못된 요청입니다."),
    PARAMETER_REQUIRED("REQUEST_002", "필수 파라미터가 누락되었습니다."),

    /**
     * 500 Internal Server Error
     */
    INTERNAL_SERVER_ERROR("SERVER_001", "서버 내부 오류가 발생했습니다."),
    EXTERNAL_API_ERROR("SERVER_002", "외부 API 호출 중 오류가 발생했습니다."),
}

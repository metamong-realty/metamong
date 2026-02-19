package com.metamong.config.exception

enum class ErrorCodes(
    val code: String,
    val message: String,
) {
    /**
     * 코드정의
     * GLOBAL_XXX : 전역적인 코드
     * REQUEST_XXX : 요청 관련
     * SERVER_XXX : 서버 관련
     */

    GLOBAL_ERROR("GLOBAL_001", "요청 처리 중 오류가 발생했습니다. 페이지를 새로 고치거나 잠시 후 다시 시도해 주세요."),

    /**
     * 400 Bad Request
     */
    INVALID_REQUEST("REQUEST_001", "잘못된 요청입니다."),
    PARAMETER_REQUIRED("REQUEST_002", "필수 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_VALUE("REQUEST_003", "파라미터 값이 올바르지 않습니다."),

    /**
     * 404 Not Found
     */
    RESOURCE_NOT_FOUND("RESOURCE_001", "요청한 리소스를 찾을 수 없습니다."),

    /**
     * 500 Internal Server Error
     */
    INTERNAL_SERVER_ERROR("SERVER_001", "서버 내부 오류가 발생했습니다."),
}

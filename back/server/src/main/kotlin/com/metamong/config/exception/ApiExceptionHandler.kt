package com.metamong.config.exception

import com.metamong.common.exception.CustomException
import com.metamong.common.response.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice(annotations = [RestController::class])
class ApiExceptionHandler {
    @ExceptionHandler(BindException::class)
    fun handleBindException(
        e: BindException,
        bindingResult: BindingResult,
    ): ResponseEntity<ApiResponse<Nothing>> {
        val defaultMessage = bindingResult.fieldError?.defaultMessage ?: "필수 값이 입력되지 않았습니다."
        val field = bindingResult.fieldError?.field ?: "Parameter"
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.badRequest("$field : $defaultMessage"))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity
            .badRequest()
            .body(ApiResponse.badRequest("요청 본문을 읽을 수 없습니다."))

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Nothing>> {
        val message = "필수 파라미터가 누락되었습니다: ${e.parameterName}"
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.badRequest(message))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        val message = "잘못된 파라미터 값: ${e.value}는(은) 유효한 ${e.propertyName} 값이 아닙니다."
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.badRequest(message))
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleHandlerMethodValidationException(e: HandlerMethodValidationException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity
            .badRequest()
            .body(ApiResponse.badRequest(e.message))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity
            .badRequest()
            .body(ApiResponse.badRequest(e.message ?: "잘못된 인자입니다."))

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity
            .status(e.status)
            .body(ApiResponse.error(e.message, e.status.value()))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error(e) { "예상치 못한 에러 발생" }
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error("요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."))
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

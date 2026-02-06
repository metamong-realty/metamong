package com.metamong.common.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val code: Int,
    val data: T? = null,
    val message: String? = null,
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> =
            ApiResponse(
                success = true,
                code = 200,
                data = data,
            )

        fun <T> ok(data: T, message: String): ApiResponse<T> =
            ApiResponse(
                success = true,
                code = 200,
                data = data,
                message = message,
            )

        fun <T> created(data: T): ApiResponse<T> =
            ApiResponse(
                success = true,
                code = 201,
                data = data,
            )

        fun <T> error(message: String, code: Int = 500): ApiResponse<T> =
            ApiResponse(
                success = false,
                code = code,
                message = message,
            )

        fun <T> badRequest(message: String): ApiResponse<T> =
            ApiResponse(
                success = false,
                code = 400,
                message = message,
            )

        fun <T> unauthorized(message: String): ApiResponse<T> =
            ApiResponse(
                success = false,
                code = 401,
                message = message,
            )

        fun <T> forbidden(message: String): ApiResponse<T> =
            ApiResponse(
                success = false,
                code = 403,
                message = message,
            )

        fun <T> notFound(message: String): ApiResponse<T> =
            ApiResponse(
                success = false,
                code = 404,
                message = message,
            )
    }
}
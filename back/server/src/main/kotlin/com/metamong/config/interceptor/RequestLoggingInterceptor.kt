package com.metamong.config.interceptor

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Body가 없는 요청(GET, DELETE, HEAD, OPTIONS)에 대한 로깅을 담당하는 Interceptor
 *
 * Body가 있는 요청(POST, PUT, PATCH)은 RequestBodyLoggingAdvice에서 처리
 */
@Component
class RequestLoggingInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val method = request.method
        val path = request.requestURI

        if (method in BODY_LESS_METHODS) {
            runCatching {
                val fullPath =
                    if (method == "GET" && !request.queryString.isNullOrEmpty()) {
                        "$path?${request.queryString}"
                    } else {
                        path
                    }
                requestLogger.info {
                    """{"path":"$fullPath","method":"$method"}"""
                }
            }
        }

        return true
    }

    companion object {
        private val requestLogger = KotlinLogging.logger("requestLogger")
        private val BODY_LESS_METHODS = listOf("GET", "DELETE", "HEAD", "OPTIONS")
    }
}

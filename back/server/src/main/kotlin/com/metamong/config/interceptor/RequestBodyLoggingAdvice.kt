package com.metamong.config.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter
import java.lang.reflect.Type

@ControllerAdvice(annotations = [RestController::class])
class RequestBodyLoggingAdvice(
    private val objectMapper: ObjectMapper,
) : RequestBodyAdviceAdapter() {
    override fun afterBodyRead(
        body: Any,
        inputMessage: HttpInputMessage,
        parameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Any {
        logging(body)
        return body
    }

    private fun logging(body: Any) {
        runCatching {
            val request =
                (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
            val path = request.servletPath
            val httpMethod = request.method
            val bodyJson = objectMapper.writeValueAsString(body)

            requestLogger.info {
                """{"path":"$path","method":"$httpMethod","body":$bodyJson}"""
            }
        }
    }

    override fun supports(
        methodParameter: MethodParameter,
        targetType: Type,
        converterType: Class<out HttpMessageConverter<*>>,
    ): Boolean = true

    companion object {
        private val requestLogger = KotlinLogging.logger("requestLogger")
    }
}

package com.metamong.common.exception

import org.springframework.http.HttpStatus

open class CustomException(
    val status: HttpStatus,
    override val message: String,
    val code: String? = null,
) : RuntimeException(message)

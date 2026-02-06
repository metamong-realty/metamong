package com.metamong.config.exception

import org.springframework.http.HttpStatus

private const val serialVersionUID: Long = 1L

abstract class BaseException : RuntimeException {
    internal var httpStatus = HttpStatus.OK
    internal var errorCode: String = ""
    internal var response: Any? = null
    override var message: String = ""

    internal constructor(message: String) : super(message) {
        this.message = message
    }

    internal constructor(errorCodes: ErrorCodes, httpStatus: HttpStatus) : super(errorCodes.message) {
        this.errorCode = errorCodes.code
        this.message = errorCodes.message
        this.httpStatus = httpStatus
    }

    internal constructor(errorCodes: ErrorCodes, httpStatus: HttpStatus, response: Any?) : super(errorCodes.message) {
        this.errorCode = errorCodes.code
        this.httpStatus = httpStatus
        this.message = errorCodes.message
        this.response = response
    }
}

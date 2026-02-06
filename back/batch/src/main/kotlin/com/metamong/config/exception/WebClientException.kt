package com.metamong.config.exception

import org.springframework.http.HttpStatus

class WebClientException : BaseException {
    constructor(message: String) : super(message) {
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    }

    constructor(errorCodes: ErrorCodes) : super(errorCodes, HttpStatus.INTERNAL_SERVER_ERROR)
    constructor(errorCodes: ErrorCodes, response: Any?) : super(errorCodes, HttpStatus.INTERNAL_SERVER_ERROR, response)
}

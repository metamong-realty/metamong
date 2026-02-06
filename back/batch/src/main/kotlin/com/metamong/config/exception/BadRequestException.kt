package com.metamong.config.exception

import org.springframework.http.HttpStatus

class BadRequestException : BaseException {
    constructor(message: String) : super(message) {
        this.httpStatus = HttpStatus.BAD_REQUEST
    }

    constructor(errorCodes: ErrorCodes) : super(errorCodes, HttpStatus.BAD_REQUEST)
    constructor(errorCodes: ErrorCodes, response: Any?) : super(errorCodes, HttpStatus.BAD_REQUEST, response)
}

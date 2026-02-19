package com.metamong.config.exception

import org.springframework.http.HttpStatus

class NotFoundException : BaseException {
    constructor(message: String) : super(message) {
        this.httpStatus = HttpStatus.NOT_FOUND
    }

    constructor(errorCodes: ErrorCodes) : super(errorCodes, HttpStatus.NOT_FOUND)
    constructor(errorCodes: ErrorCodes, response: Any?) : super(errorCodes, HttpStatus.NOT_FOUND, response)
}

package com.beat_it.global.error

import com.beat_it.global.response.BasicResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<BasicResponse<Nothing>> {
        val errorCode = e.errorCode

        return ResponseEntity
            .status(errorCode.status)
            .body(BasicResponse.fail(errorCode.code, errorCode.message))
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<BasicResponse<Nothing>> {
        e.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BasicResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.code, e.message ?: "Internal Server Error"))
    }
}
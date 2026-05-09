package com.beat_it.global.error

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val response = ErrorResponse.of(errorCode)

        return ResponseEntity(response, errorCode.status)
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        e.printStackTrace()

        val response = ErrorResponse(
            status = 500,
            code = "COMMON 001",
            message = e.message ?: "Internal Server Error"
        )
        return ResponseEntity(response, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
package com.beat_it.global.error

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String
) {
    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                code = errorCode.code,
                message = errorCode.message
            )
        }
    }
}
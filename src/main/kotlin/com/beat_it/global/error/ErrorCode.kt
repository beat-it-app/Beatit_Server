package com.beat_it.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // 예시 에러 코드
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CUSTOM 001", "올바르지 않은 입력값입니다.")
}
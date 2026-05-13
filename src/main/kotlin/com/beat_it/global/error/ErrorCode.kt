 package com.beat_it.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // 예시 에러 코드
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CUSTOM 001", "올바르지 않은 입력값입니다."),

    IDENTIFIER_DUPLICATED(HttpStatus.BAD_REQUEST, "SIGNUP 001", "이미 사용 중인 아이디입니다."),
    EMAIL_VERIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP 002", "이메일 인증번호 발송에 실패했습니다."),
    EMAIL_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "SIGNUP 003", "이메일 인증에 실패했습니다."),
}
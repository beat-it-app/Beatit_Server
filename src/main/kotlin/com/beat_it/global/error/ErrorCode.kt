 package com.beat_it.global.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // 예시 에러 코드
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "CUSTOM 001", "올바르지 않은 입력값입니다."),

    // 회원가입
    IDENTIFIER_DUPLICATED(HttpStatus.BAD_REQUEST, "SIGNUP 001", "이미 사용 중인 아이디입니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP 002", "이메일 인증번호 발송에 실패했습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "SIGNUP 003", "인증 시간이 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "SIGNUP 004", "잘못된 인증번호입니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "SIGNUP 005", "진행 중인 인증 요청을 찾을 수 없습니다.")
}
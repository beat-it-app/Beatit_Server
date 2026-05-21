package com.beat_it.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SignUpRequest (
    @Schema(description = "유저 아이디", example = "user1")
    val identifier: String?,

    @Schema(description = "비밀번호", example = "password123!")
    val password: String?,

    @Schema(description = "사용자 이메일", example = "user@example.com")
    val email: String
)
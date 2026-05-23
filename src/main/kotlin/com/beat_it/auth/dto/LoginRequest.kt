package com.beat_it.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class LoginRequest(
    @Schema(description = "아이디", example = "user1")
    val identifier: String,

    @Schema(description = "비밀번호", example = "password123!")
    val password: String,

    @Schema(description = "자동 로그인 여부", example = false.toString())
    val rememberMe: Boolean
)

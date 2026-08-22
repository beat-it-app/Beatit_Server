package com.beat_it.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class ResetPasswordResponse(
    @Schema(description = "변경된 비밀번호", example = "newpassword123!")
    val password: String
)

package com.beat_it.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class FindIdentifierResponse(
    @Schema(description = "찾은 유저 아이디", example = "user1")
    val identifier: String
)

package com.beat_it.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class GoogleLoginRequest(
    @Schema(description = "구글 앱 SDK에서 발급받은 ID Token", required = true)
    val idToken: String
)

package com.beat_it.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SocialLoginRequest(
    @Schema(description = "구글 앱 SDK에서 발급받은 ID 토큰", required = true)
    val idToken: String
)

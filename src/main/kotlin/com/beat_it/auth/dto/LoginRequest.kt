package com.beat_it.auth.dto

import com.beat_it.notification.entity.enum.PlatformType
import io.swagger.v3.oas.annotations.media.Schema

data class LoginRequest(
    @Schema(description = "아이디", example = "user1")
    val identifier: String,

    @Schema(description = "비밀번호", example = "password123!")
    val password: String,

    @Schema(description = "자동 로그인 여부", example = false.toString())
    val rememberMe: Boolean,

    @Schema(description = "디바이스 고유 ID (선택: 푸시 알림 등록용)", example = "device-uuid-1234", required = false)
    val deviceId: String? = null,

    @Schema(description = "플랫폼 타입 (선택: IOS, ANDROID)", example = "ANDROID", required = false)
    val platformType: PlatformType? = null,

    @Schema(description = "FCM 기기 푸시 토큰 (선택)", example = "fcm_token_string...", required = false)
    val pushToken: String? = null,

    @Schema(description = "앱 버전 (선택)", example = "1.0.0", required = false)
    val appVersion: String? = null
)

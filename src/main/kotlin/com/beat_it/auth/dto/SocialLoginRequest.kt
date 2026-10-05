package com.beat_it.auth.dto

import com.beat_it.notification.entity.enum.PlatformType
import io.swagger.v3.oas.annotations.media.Schema

data class GoogleLoginRequest(
    @Schema(description = "구글 앱 SDK에서 발급받은 ID Token", required = true)
    val idToken: String,
    @Schema(description = "디바이스 고유 ID (선택: 푸시 알림 등록용)", required = false)
    val deviceId: String? = null,
    @Schema(description = "플랫폼 타입 (선택: IOS, ANDROID)", required = false)
    val platformType: PlatformType? = null,
    @Schema(description = "FCM 기기 푸시 토큰 (선택)", required = false)
    val pushToken: String? = null,
    @Schema(description = "앱 버전 (선택)", required = false)
    val appVersion: String? = null
)

data class KakaoLoginRequest(
    @Schema(description = "카카오 앱 SDK에서 발급받은 Access Token", required = true)
    val accessToken: String,
    @Schema(description = "디바이스 고유 ID (선택: 푸시 알림 등록용)", required = false)
    val deviceId: String? = null,
    @Schema(description = "플랫폼 타입 (선택: IOS, ANDROID)", required = false)
    val platformType: PlatformType? = null,
    @Schema(description = "FCM 기기 푸시 토큰 (선택)", required = false)
    val pushToken: String? = null,
    @Schema(description = "앱 버전 (선택)", required = false)
    val appVersion: String? = null
)

data class NaverLoginRequest(
    @Schema(description = "네이버 앱 SDK에서 발급받은 Access Token", required = true)
    val accessToken: String,
    @Schema(description = "디바이스 고유 ID (선택: 푸시 알림 등록용)", required = false)
    val deviceId: String? = null,
    @Schema(description = "플랫폼 타입 (선택: IOS, ANDROID)", required = false)
    val platformType: PlatformType? = null,
    @Schema(description = "FCM 기기 푸시 토큰 (선택)", required = false)
    val pushToken: String? = null,
    @Schema(description = "앱 버전 (선택)", required = false)
    val appVersion: String? = null
)
package com.beat_it.auth.dto

import com.beat_it.notification.entity.enum.PlatformType
import com.fasterxml.jackson.annotation.JsonProperty
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
    val appVersion: String? = null,
    @Schema(description = "탈퇴 유예 계정 복구 여부 (복구 확인 팝업에서 '복구하기' 클릭 시 true 전달)", example = "false", required = false)
    val isRestore: Boolean = false
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
    val appVersion: String? = null,
    @Schema(description = "탈퇴 유예 계정 복구 여부 (복구 확인 팝업에서 '복구하기' 클릭 시 true 전달)", example = "false", required = false)
    val isRestore: Boolean = false
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
    val appVersion: String? = null,
    @Schema(description = "탈퇴 유예 계정 복구 여부 (복구 확인 팝업에서 '복구하기' 클릭 시 true 전달)", example = "false", required = false)
    val isRestore: Boolean = false
)

data class AppleLoginRequest(
    @Schema(description = "애플 SDK에서 발급받은 identityToken (JWT)", required = true)
    val identityToken: String,
    @Schema(description = "사용자 이메일 (애플 최초 로그인 시 전달되는 값, 없을 시 토큰 클레임 사용)", required = false)
    val email: String? = null,
    @Schema(description = "디바이스 고유 ID (선택: 푸시 알림 등록용)", required = false)
    val deviceId: String? = null,
    @Schema(description = "플랫폼 타입 (선택: IOS, ANDROID)", required = false)
    val platformType: PlatformType? = null,
    @Schema(description = "FCM 기기 푸시 토큰 (선택)", required = false)
    val pushToken: String? = null,
    @Schema(description = "앱 버전 (선택)", required = false)
    val appVersion: String? = null,
    @Schema(description = "탈퇴 유예 계정 복구 여부 (복구 확인 팝업에서 '복구하기' 클릭 시 true 전달)", example = "false", required = false)
    val isRestore: Boolean = false
)

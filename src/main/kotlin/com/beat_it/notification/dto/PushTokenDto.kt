package com.beat_it.notification.dto

import com.beat_it.notification.entity.enum.PlatformType
import jakarta.validation.constraints.NotBlank

data class PushTokenRegisterRequest(
    val deviceId: String,
    val platformType: PlatformType,
    val pushToken: String,
    val appVersion: String? = null
)

data class PushTokenResponse(
    val pushTokenId: Long,
    val deviceId: String,
    val platformType: PlatformType,
    val isActive: Boolean
)

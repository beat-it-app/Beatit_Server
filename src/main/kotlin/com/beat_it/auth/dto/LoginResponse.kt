package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.Role
import com.beat_it.auth.entity.enum.SocialProvider

data class LoginResponse(
    val userId: Long?,
    val role: Role,
    val isCreatedProfile: Boolean,
    val socialProvider: SocialProvider? = null
)



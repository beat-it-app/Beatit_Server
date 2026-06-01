package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.Role

data class LoginResponse(
    val userId: Long?,
    val role: Role,
    val isCreatedProfile: Boolean
)


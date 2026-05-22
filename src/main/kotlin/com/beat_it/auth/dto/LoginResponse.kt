package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.Role
import java.util.UUID

data class LoginResponse(
    val userId: Long?,
    val userPublicId: String?,
    // val teamPublicId: String?,
    val role: Role,
    val isCreatedProfile: Boolean
)


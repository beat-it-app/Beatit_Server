package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.Role
import java.util.UUID

data class LoginResponse(
    val publicId: UUID,
    val role: Role,
    val accessToken: String
)

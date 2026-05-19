package com.beat_it.auth.dto

import java.util.UUID

data class LoginResponse(
    val publicId: UUID,
    val identifier: String,
    val token: String
)

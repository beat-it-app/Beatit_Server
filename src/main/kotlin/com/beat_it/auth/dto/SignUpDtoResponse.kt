package com.beat_it.auth.dto

import java.time.OffsetDateTime

data class SignUpDtoResponse (
    val userId: Long,
    val identifier: String,
    val email: String,
    val createdAt: OffsetDateTime
)
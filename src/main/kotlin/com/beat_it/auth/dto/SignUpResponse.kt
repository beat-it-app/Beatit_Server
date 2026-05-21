package com.beat_it.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class SignUpResponse (
    val userId: Long?,
    val identifier: String?,
    val email: String,

    @JsonProperty("created_at")
    val createdAt: OffsetDateTime
) 
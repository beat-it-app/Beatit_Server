package com.beat_it.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.UUID

data class SignUpResponse (
    val publicId: UUID,
    val identifier: String?,
    val email: String,

    @JsonProperty("created_at")
    val createdAt: OffsetDateTime,

    @JsonProperty("access_token")
    val accessToken: String
) 
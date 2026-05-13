package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.AccountStatus
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Getter
import java.time.OffsetDateTime

@Getter
data class SignUpRequest (
    @JsonProperty("access_status")
    val accountStatus: AccountStatus,

    @JsonProperty("created_at")
    val createdAt: OffsetDateTime,


    val identifier: String,

    val password: String,

    val email: String,

    @JsonProperty("social_id")
    val socialId: String?,


    val timezone: String?,
)
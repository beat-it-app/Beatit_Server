package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.entity.enum.SocialProvider
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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


    @Enumerated(EnumType.STRING)
    val provider: SocialProvider,

    @JsonProperty("social_id")
    val socialId: String?,
)
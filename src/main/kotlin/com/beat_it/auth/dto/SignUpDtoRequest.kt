package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.AccountStatus
import lombok.Getter
import java.time.OffsetDateTime
import java.util.UUID

@Getter
data class SignUpDtoRequest (
    val accountStatus: AccountStatus,
    val createdAt: OffsetDateTime,

    val identifier: String,
    val password: String,
    val email: String,
    val socialId: String?,

    val timezone: String?,
)
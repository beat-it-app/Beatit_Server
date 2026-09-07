package com.beat_it.team.dto

import java.time.OffsetDateTime
import java.util.UUID

data class TeamWithdrawalResponse(
    val teamPublicId: UUID,
    val userId: Long,
    val requestedAt: OffsetDateTime,
    val scheduledDeletionDate: OffsetDateTime
)

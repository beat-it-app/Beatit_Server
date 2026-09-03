package com.beat_it.team.dto

import java.util.UUID

data class TeamWithdrawalResponse(
    val teamPublicId: UUID,
    val userId: Long,
    val requestedAt: String,
    val scheduledDeletionDate: String
)

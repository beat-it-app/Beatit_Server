package com.beat_it.auth.dto

import java.time.OffsetDateTime

data class WithdrawalResponse(
    val userId: Long,
    val requestedAt: OffsetDateTime,
    val scheduledDeletionDate: OffsetDateTime
)

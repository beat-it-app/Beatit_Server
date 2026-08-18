package com.beat_it.auth.dto

data class WithdrawalResponse(
    val userId: Long,
    val requestedAt: String,
    val scheduledDeletionDate: String
)

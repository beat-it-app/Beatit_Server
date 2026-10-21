package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.WithdrawalReason
import jakarta.validation.constraints.NotNull

data class WithdrawalRequest(
    @field:NotNull(message = "탈퇴 사유는 필수입니다.")
    val withdrawalReason: WithdrawalReason,
    val detailReason: String? = null
)

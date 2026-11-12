package com.beat_it.auth.entity

import com.beat_it.auth.entity.enum.WithdrawalReason
import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_withdrawal_reasons")
class UserWithdrawalReason(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_withdrawal_reason_id")
    val userWithdrawalReasonId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "withdrawal_reason", nullable = false)
    val withdrawalReason: WithdrawalReason,

    @Column(name = "detail_reason", columnDefinition = "TEXT")
    val detailReason: String? = null
) : BaseCreatedTimeEntity() {
    companion object {
        fun of(
            userId: Long,
            withdrawalReason: WithdrawalReason,
            detailReason: String? = null
        ): UserWithdrawalReason {
            return UserWithdrawalReason(
                userId = userId,
                withdrawalReason = withdrawalReason,
                detailReason = detailReason
            )
        }
    }
}

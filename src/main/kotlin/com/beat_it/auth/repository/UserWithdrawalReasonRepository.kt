package com.beat_it.auth.repository

import com.beat_it.auth.entity.UserWithdrawalReason
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserWithdrawalReasonRepository : JpaRepository<UserWithdrawalReason, Long> {
    fun findAllByUserId(userId: Long): List<UserWithdrawalReason>
}

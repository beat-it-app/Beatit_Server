package com.beat_it.auth.repository

import com.beat_it.auth.entity.UserAuthAccounts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAuthAccountRepository : JpaRepository<UserAuthAccounts, Long> {
    fun findByIdentifier(identifier: String): UserAuthAccounts?
}

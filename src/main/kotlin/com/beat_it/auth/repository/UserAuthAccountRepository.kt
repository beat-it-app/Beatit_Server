package com.beat_it.auth.repository

import com.beat_it.auth.entity.UserAuthAccounts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserAuthAccountRepository : JpaRepository<UserAuthAccounts, Long> {
    fun findByIdentifier(identifier: String): UserAuthAccounts?
    fun findByUserUserId(userId: Long): UserAuthAccounts?
    @Query("select u from UserAuthAccounts u join fetch u.user where u.googleId = :googleId")
    fun findByGoogleId(googleId: String): UserAuthAccounts?
    @Query("select u from UserAuthAccounts u join fetch u.user where u.kakaoId = :kakaoId")
    fun findByKakaoId(kakaoId: String): UserAuthAccounts?
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserAuthAccounts?
    fun findByIdentifierAndEmail(identifier: String, email: String): UserAuthAccounts?
}

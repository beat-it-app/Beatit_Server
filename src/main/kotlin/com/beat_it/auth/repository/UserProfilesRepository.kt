package com.beat_it.auth.repository

import com.beat_it.auth.entity.UserProfiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProfilesRepository : JpaRepository<UserProfiles, Long> {
    fun existsByUser_UserId(userId: Long?): Boolean
    fun findByUserUserId(userId: Long): UserProfiles?
}

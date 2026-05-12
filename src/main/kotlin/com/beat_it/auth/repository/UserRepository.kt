package com.beat_it.auth.repository

import com.beat_it.auth.entity.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<Users, Long> {
//    fun findByUserId(userId: Long): Users?
    fun findByIdentifier(identifier: String): Users?
}
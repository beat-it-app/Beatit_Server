package com.beat_it.auth.repository

import com.beat_it.auth.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<Users, Long> {
    fun findByPublicId(publicId: UUID): Users?
}

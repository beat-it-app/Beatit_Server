package com.beat_it.auth.repository

import com.beat_it.auth.entity.AuthFiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthFilesRepository : JpaRepository<AuthFiles, Long> {
    fun findAllByUser_UserId(userId: Long): List<AuthFiles>
}
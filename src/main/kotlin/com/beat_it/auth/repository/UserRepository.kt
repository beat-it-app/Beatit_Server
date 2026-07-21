package com.beat_it.auth.repository

import com.beat_it.auth.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<Users, Long> {
    fun findByPublicId(publicId: UUID): Users?

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(
        """
        UPDATE Users u
        SET u.currentTeamId = null
        WHERE u.currentTeamId = :teamId
        """
    )
    fun clearCurrentTeamIdByTeamId(
        @Param("teamId") teamId: Long
    ): Int

    fun findByUserIdIn(userIds: List<Long>): List<Users>
}

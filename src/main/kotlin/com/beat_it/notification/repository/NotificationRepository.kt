package com.beat_it.notification.repository

import com.beat_it.notification.entity.Notifications
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NotificationRepository : JpaRepository<Notifications, Long> {
    fun findByUserIdAndTeamId(userId: Long, teamId: Long, pageable: Pageable): Page<Notifications>

    fun countByUserIdAndTeamIdAndIsReadFalse(userId: Long, teamId: Long): Long

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.userId = :userId AND n.teamId = :teamId AND n.isRead = false")
    fun markAllAsReadByUserIdAndTeamId(
        @Param("userId") userId: Long,
        @Param("teamId") teamId: Long
    ): Int
}

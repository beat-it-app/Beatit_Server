package com.beat_it.notification.repository

import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.template.NotificationCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NotificationRepository : JpaRepository<Notifications, Long> {
    fun findByUserIdAndTeamId(userId: Long, teamId: Long, pageable: Pageable): Page<Notifications>

    fun findByUserIdAndTeamIdAndCategoryNot(
        userId: Long,
        teamId: Long,
        category: NotificationCategory,
        pageable: Pageable
    ): Page<Notifications>

    fun countByUserIdAndTeamIdAndIsReadFalse(userId: Long, teamId: Long): Long

    fun countByUserIdAndTeamIdAndCategoryNotAndIsReadFalse(
        userId: Long,
        teamId: Long,
        category: NotificationCategory
    ): Long

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.userId = :userId AND n.teamId = :teamId AND n.isRead = false")
    fun markAllAsReadByUserIdAndTeamId(
        @Param("userId") userId: Long,
        @Param("teamId") teamId: Long
    ): Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.userId = :userId AND n.teamId = :teamId AND n.category != :category AND n.isRead = false")
    fun markAllAsReadByUserIdAndTeamIdAndCategoryNot(
        @Param("userId") userId: Long,
        @Param("teamId") teamId: Long,
        @Param("category") category: NotificationCategory
    ): Int
}

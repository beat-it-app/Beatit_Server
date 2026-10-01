package com.beat_it.notification.repository

import com.beat_it.notification.entity.Notifications
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NotificationRepository : JpaRepository<Notifications, Long> {

    // 특정 유저 & 특정 팀의 알림 페이징 목록 조회 (최신순 등 Pageable 적용)
    fun findByUserIdAndTeamId(userId: Long, teamId: Long, pageable: Pageable): Page<Notifications>

    // 특정 유저 & 특정 팀의 읽지 않은 알림 개수
    fun countByUserIdAndTeamIdAndIsReadFalse(userId: Long, teamId: Long): Long

    // 특정 유저 & 특정 팀의 읽지 않은 알림 존재 여부
    fun existsByUserIdAndTeamIdAndIsReadFalse(userId: Long, teamId: Long): Boolean

    // 본인의 특정 알림 단건 조회
    fun findByNotificationIdAndUserId(notificationId: Long, userId: Long): Notifications?

    // 특정 유저 & 특정 팀의 모든 알림 일괄 읽음 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.userId = :userId AND n.teamId = :teamId AND n.isRead = false")
    fun markAllAsReadByUserIdAndTeamId(
        @Param("userId") userId: Long,
        @Param("teamId") teamId: Long
    ): Int
}

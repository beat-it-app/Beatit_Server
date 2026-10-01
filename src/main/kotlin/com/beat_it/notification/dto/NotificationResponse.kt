package com.beat_it.notification.dto

import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.template.NotificationCategory
import com.beat_it.notification.template.NotificationType
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class NotificationItemResponse(
    val notificationId: Long,
    val category: NotificationCategory,
    val type: NotificationType,
    val title: String,
    val content: String,
    val directTo: String?,
    val targetId: Long?,
    val isRead: Boolean,
    val createdAt: OffsetDateTime,
    val relativeTime: String
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd")

        fun from(notification: Notifications, now: OffsetDateTime = OffsetDateTime.now()): NotificationItemResponse {
            return NotificationItemResponse(
                notificationId = notification.notificationId ?: 0L,
                category = notification.category,
                type = notification.notificationType,
                title = notification.title,
                content = notification.content,
                directTo = notification.directTo,
                targetId = notification.targetId,
                isRead = notification.isRead,
                createdAt = notification.createdAt,
                relativeTime = formatRelativeTime(notification.createdAt, now)
            )
        }

        private fun formatRelativeTime(dateTime: OffsetDateTime, now: OffsetDateTime): String {
            val duration = Duration.between(dateTime, now)
            val seconds = duration.seconds

            return when {
                seconds < 60 -> "방금 전"
                seconds < 3600 -> "${seconds / 60}분 전"
                seconds < 86400 -> "${seconds / 3600}시간 전"
                seconds < 86400 * 7 -> "${seconds / 86400}일 전"
                else -> dateTime.format(DATE_FORMATTER)
            }
        }
    }
}

data class NotificationPageResponse(
    val teamId: Long,
    val teamName: String,
    val unreadCount: Long,
    val hasUnread: Boolean,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isLast: Boolean,
    val notifications: List<NotificationItemResponse>
)

data class NotificationUnreadStatusResponse(
    val teamId: Long,
    val unreadCount: Long,
    val hasUnread: Boolean
)

/**
 * 알림 탭(클릭) 시 direct 이동 및 팀 전환 응답 DTO
 */
data class NotificationClickResponse(
    val notificationId: Long,
    val targetTeamId: Long,
    val targetTeamName: String,
    val directTo: String?,
    val targetId: Long?,
    val isRead: Boolean
)

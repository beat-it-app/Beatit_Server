package com.beat_it.notification.dto

import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.template.NotificationCategory
import com.beat_it.notification.template.NotificationType
import java.time.OffsetDateTime

data class NotificationItemResponse(
    val notificationId: Long,
    val category: NotificationCategory,
    val type: NotificationType,
    val title: String,
    val content: String,
    val directTo: String?,
    val targetId: Long?,
    val isRead: Boolean,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(notification: Notifications): NotificationItemResponse {
            return NotificationItemResponse(
                notificationId = notification.notificationId ?: 0L,
                category = notification.category,
                type = notification.notificationType,
                title = notification.title,
                content = notification.content,
                directTo = notification.directTo,
                targetId = notification.targetId,
                isRead = notification.isRead,
                createdAt = notification.createdAt
            )
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

data class NotificationClickResponse(
    val notificationId: Long,
    val targetTeamId: Long,
    val targetTeamName: String,
    val directTo: String?,
    val targetId: Long?,
    val isRead: Boolean
)

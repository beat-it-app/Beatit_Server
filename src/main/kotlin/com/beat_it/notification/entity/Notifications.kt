package com.beat_it.notification.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.notification.template.NotificationCategory
import com.beat_it.notification.template.NotificationType
import jakarta.persistence.*

@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notifications_user_team", columnList = "user_id, team_id, created_at"),
        Index(name = "idx_notifications_user_team_unread", columnList = "user_id, team_id, is_read")
    ]
)
class Notifications(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    val notificationId: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: NotificationType,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    val category: NotificationCategory,

    @Column(name = "title", nullable = false, length = 200)
    val title: String,

    @Column(name = "content", nullable = false, length = 1000)
    val content: String,

    @Column(name = "push_text", nullable = false, length = 1000)
    val pushText: String,

    @Column(name = "direct_to", length = 500)
    val directTo: String? = null,

    @Column(name = "target_id")
    val targetId: Long? = null,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false,

    @Column(name = "is_push", nullable = false)
    val isPush: Boolean = true,

    @Column(name = "is_toast", nullable = false)
    val isToast: Boolean = true,
) : BaseCreatedTimeEntity() {

    fun markAsRead() {
        this.isRead = true
    }
}

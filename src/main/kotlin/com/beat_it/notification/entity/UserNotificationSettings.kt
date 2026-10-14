package com.beat_it.notification.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.notification.template.NotificationMessage
import com.beat_it.notification.template.NotificationType
import jakarta.persistence.*

@Entity
@Table(name = "user_notification_settings")
class UserNotificationSettings(
    @Id
    @Column(name = "user_id")
    val userId: Long,

    // 마스터 전체 알림
    @Column(name = "all_notification", nullable = false)
    var allNotification: Boolean = true,

    // 1. 일정 및 활동
    @Column(name = "activity_section", nullable = false)
    var activitySection: Boolean = true,

    @Column(name = "calendar_schedule", nullable = false)
    var calendarSchedule: Boolean = true,

    @Column(name = "team_activity_space", nullable = false)
    var teamActivitySpace: Boolean = true,

    @Column(name = "activity_comment_mention", nullable = false)
    var activityCommentMention: Boolean = true,

    // 2. 공지 및 협업
    @Column(name = "collaboration_section", nullable = false)
    var collaborationSection: Boolean = true,

    @Column(name = "notice", nullable = false)
    var notice: Boolean = true,

    @Column(name = "poll_and_meetit", nullable = false)
    var pollAndMeetit: Boolean = true,

    @Column(name = "progress_result", nullable = false)
    var progressResult: Boolean = true,

    @Column(name = "collaboration_comment_mention", nullable = false)
    var collaborationCommentMention: Boolean = true,

    // 3. 채팅
    @Column(name = "chat_message", nullable = false)
    var chatMessage: Boolean = true,

    // 4. 팀 관리
    @Column(name = "team_manage_section", nullable = false)
    var teamManageSection: Boolean = true,

    @Column(name = "position_change", nullable = false)
    var positionChange: Boolean = true,

    @Column(name = "member_change", nullable = false)
    var memberChange: Boolean = true // 운영진 전용
) : BaseUpdatedTimeEntity() {

    fun updateAll(enabled: Boolean) {
        this.allNotification = enabled
        this.activitySection = enabled
        this.calendarSchedule = enabled
        this.teamActivitySpace = enabled
        this.activityCommentMention = enabled

        this.collaborationSection = enabled
        this.notice = enabled
        this.pollAndMeetit = enabled
        this.progressResult = enabled
        this.collaborationCommentMention = enabled

        this.chatMessage = enabled

        this.teamManageSection = enabled
        this.positionChange = enabled
        this.memberChange = enabled
    }

    fun updateActivitySection(enabled: Boolean) {
        this.activitySection = enabled
        this.calendarSchedule = enabled
        this.teamActivitySpace = enabled
        this.activityCommentMention = enabled
    }

    fun updateCollaborationSection(enabled: Boolean) {
        this.collaborationSection = enabled
        this.notice = enabled
        this.pollAndMeetit = enabled
        this.progressResult = enabled
        this.collaborationCommentMention = enabled
    }

    fun updateTeamManageSection(enabled: Boolean) {
        this.teamManageSection = enabled
        this.positionChange = enabled
        this.memberChange = enabled
    }

    fun isNotificationEnabled(message: NotificationMessage): Boolean {
        // 0. 마스터 전체 알림이 OFF이면 모든 알림 차단
        if (!this.allNotification) return false

        // 1. [일정 및 활동] 섹션 알림 판정
        if (isActivityNotification(message)) {
            return isActivityNotificationEnabled(message)
        }

        // 2. [공지 및 협업] 섹션 알림 판정
        if (isCollaborationNotification(message)) {
            return isCollaborationNotificationEnabled(message)
        }

        // 3. [채팅] 판정
        if (message.type == NotificationType.CHAT_MESSAGE) {
            return this.chatMessage
        }

        // 4. [팀 관리] 섹션 알림 판정
        if (isTeamManageNotification(message)) {
            return isTeamManageNotificationEnabled(message)
        }

        return true
    }

    // 1. [일정 및 활동] 섹션
    private fun isActivityNotification(message: NotificationMessage): Boolean {
        return message.type == NotificationType.SCHEDULE_REGISTERED ||
                message.type == NotificationType.SCHEDULE_REMINDER ||
                message.type == NotificationType.STUDIO_REGISTERED ||
                isArchiveCommentOrMention(message)
    }

    private fun isActivityNotificationEnabled(message: NotificationMessage): Boolean {
        if (!this.activitySection) return false
        return when {
            message.type == NotificationType.SCHEDULE_REGISTERED ||
            message.type == NotificationType.SCHEDULE_REMINDER -> this.calendarSchedule

            message.type == NotificationType.STUDIO_REGISTERED -> this.teamActivitySpace

            isArchiveCommentOrMention(message) -> this.activityCommentMention

            else -> true
        }
    }

    // 2. [공지 및 협업] 섹션
    private fun isCollaborationNotification(message: NotificationMessage): Boolean {
        return message.type == NotificationType.NOTICE_REGISTERED ||
                message.type == NotificationType.POLL_CREATED ||
                message.type == NotificationType.POLL_DEADLINE_APPROACHING ||
                message.type == NotificationType.POLL_REMINDER ||
                message.type == NotificationType.POLL_COMPLETED ||
                message.type == NotificationType.MEETIT_CREATED ||
                message.type == NotificationType.MEETIT_REMINDER ||
                message.type == NotificationType.MEETIT_COMPLETED ||
                isCollaborationCommentOrMention(message)
    }

    private fun isCollaborationNotificationEnabled(message: NotificationMessage): Boolean {
        if (!this.collaborationSection) return false
        return when (message.type) {
            NotificationType.NOTICE_REGISTERED -> this.notice

            NotificationType.POLL_CREATED,
            NotificationType.POLL_DEADLINE_APPROACHING,
            NotificationType.POLL_REMINDER,
            NotificationType.MEETIT_CREATED,
            NotificationType.MEETIT_REMINDER -> this.pollAndMeetit

            NotificationType.POLL_COMPLETED,
            NotificationType.MEETIT_COMPLETED -> this.progressResult

            NotificationType.COMMENT_CREATED,
            NotificationType.COMMENT_MENTIONED -> this.collaborationCommentMention

            else -> true
        }
    }

    // 4. [팀 관리] 섹션
    private fun isTeamManageNotification(message: NotificationMessage): Boolean {
        return message.type == NotificationType.PART_ASSIGNED ||
                message.type == NotificationType.MEMBER_JOIN ||
                message.type == NotificationType.MEMBER_LEAVE
    }

    private fun isTeamManageNotificationEnabled(message: NotificationMessage): Boolean {
        if (!this.teamManageSection) return false
        return when (message.type) {
            NotificationType.PART_ASSIGNED -> this.positionChange
            NotificationType.MEMBER_JOIN,
            NotificationType.MEMBER_LEAVE -> this.memberChange
            else -> true
        }
    }

    // 헬퍼: 아카이브 댓글/멘션 vs 공지/투표 댓글/멘션 구분
    private fun isArchiveCommentOrMention(message: NotificationMessage): Boolean {
        return (message.type == NotificationType.COMMENT_CREATED || message.type == NotificationType.COMMENT_MENTIONED) &&
                message.directTo?.startsWith("/archives") == true
    }

    private fun isCollaborationCommentOrMention(message: NotificationMessage): Boolean {
        return (message.type == NotificationType.COMMENT_CREATED || message.type == NotificationType.COMMENT_MENTIONED) &&
                message.directTo?.startsWith("/archives") != true
    }
}

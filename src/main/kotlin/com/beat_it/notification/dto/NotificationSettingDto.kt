package com.beat_it.notification.dto

import com.beat_it.notification.entity.UserNotificationSettings

data class NotificationSettingsResponse(
    val allNotification: Boolean,

    // 1. 일정 및 활동
    val activitySection: Boolean,
    val calendarSchedule: Boolean,
    val teamActivitySpace: Boolean,
    val activityCommentMention: Boolean,

    // 2. 공지 및 협업
    val collaborationSection: Boolean,
    val notice: Boolean,
    val pollAndMeetit: Boolean,
    val progressResult: Boolean,
    val collaborationCommentMention: Boolean,

    // 3. 채팅
    val chatMessage: Boolean,

    // 4. 팀 관리
    val teamManageSection: Boolean,
    val positionChange: Boolean,
    val memberChange: Boolean?,

    // 권한 플래그
    val isManager: Boolean // 운영진 여부
) {
    companion object {
        fun of(settings: UserNotificationSettings, isManager: Boolean): NotificationSettingsResponse {
            return NotificationSettingsResponse(
                allNotification = settings.allNotification,
                activitySection = settings.activitySection,
                calendarSchedule = settings.calendarSchedule,
                teamActivitySpace = settings.teamActivitySpace,
                activityCommentMention = settings.activityCommentMention,
                collaborationSection = settings.collaborationSection,
                notice = settings.notice,
                pollAndMeetit = settings.pollAndMeetit,
                progressResult = settings.progressResult,
                collaborationCommentMention = settings.collaborationCommentMention,
                chatMessage = settings.chatMessage,
                teamManageSection = settings.teamManageSection,
                positionChange = settings.positionChange,
                memberChange = if (isManager) settings.memberChange else null,
                isManager = isManager
            )
        }
    }
}

data class NotificationSettingsUpdateRequest(
    val allNotification: Boolean? = null,

    // 1. 일정 및 활동
    val activitySection: Boolean? = null,
    val calendarSchedule: Boolean? = null,
    val teamActivitySpace: Boolean? = null,
    val activityCommentMention: Boolean? = null,

    // 2. 공지 및 협업
    val collaborationSection: Boolean? = null,
    val notice: Boolean? = null,
    val pollAndMeetit: Boolean? = null,
    val progressResult: Boolean? = null,
    val collaborationCommentMention: Boolean? = null,

    // 3. 채팅
    val chatMessage: Boolean? = null,

    // 4. 팀 관리
    val teamManageSection: Boolean? = null,
    val positionChange: Boolean? = null,
    val memberChange: Boolean? = null
)

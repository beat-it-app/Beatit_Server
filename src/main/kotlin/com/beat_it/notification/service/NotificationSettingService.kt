package com.beat_it.notification.service

import com.beat_it.auth.service.UserService
import com.beat_it.notification.dto.NotificationSettingsResponse
import com.beat_it.notification.dto.NotificationSettingsUpdateRequest
import com.beat_it.notification.entity.UserNotificationSettings
import com.beat_it.notification.repository.UserNotificationSettingRepository
import com.beat_it.notification.template.NotificationMessage
import com.beat_it.notification.template.NotificationType
import com.beat_it.team.entity.enum.TeamRole
import com.beat_it.team.repository.TeamMembershipRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationSettingService(
    private val settingRepository: UserNotificationSettingRepository,
    private val userService: UserService,
    private val teamMembershipRepository: TeamMembershipRepository
) {

    @Transactional
    fun getSettings(userId: Long): NotificationSettingsResponse {
        val settings = getOrCreateSettings(userId)
        val isManager = checkIsManager(userId)
        return NotificationSettingsResponse.of(settings, isManager)
    }

    @Transactional
    fun updateSettings(userId: Long, request: NotificationSettingsUpdateRequest): NotificationSettingsResponse {
        val settings = getOrCreateSettings(userId)

        request.allNotification?.let { allEnabled ->
            if (!allEnabled) {
                settings.updateAll(false)
            } else {
                settings.allNotification = true
                if (request.activitySection == null &&
                    request.collaborationSection == null &&
                    request.teamManageSection == null
                ) {
                    settings.updateAll(true)
                }
            }
        }

        // 2. 일정 및 활동 섹션
        request.activitySection?.let { sectionEnabled ->
            settings.updateActivitySection(sectionEnabled)
        }
        request.calendarSchedule?.let {
            settings.calendarSchedule = it
            if (it) settings.activitySection = true
        }
        request.teamActivitySpace?.let {
            settings.teamActivitySpace = it
            if (it) settings.activitySection = true
        }
        request.activityCommentMention?.let {
            settings.activityCommentMention = it
            if (it) settings.activitySection = true
        }

        // 3. 공지 및 협업 섹션
        request.collaborationSection?.let { sectionEnabled ->
            settings.updateCollaborationSection(sectionEnabled)
        }
        request.notice?.let {
            settings.notice = it
            if (it) settings.collaborationSection = true
        }
        request.pollAndMeetit?.let {
            settings.pollAndMeetit = it
            if (it) settings.collaborationSection = true
        }
        request.progressResult?.let {
            settings.progressResult = it
            if (it) settings.collaborationSection = true
        }
        request.collaborationCommentMention?.let {
            settings.collaborationCommentMention = it
            if (it) settings.collaborationSection = true
        }

        // 4. 채팅
        request.chatMessage?.let {
            settings.chatMessage = it
        }

        val isManager = checkIsManager(userId)

        // 5. 팀 관리 섹션 처리
        request.teamManageSection?.let { sectionEnabled ->
            settings.updateTeamManageSection(sectionEnabled)
        }
        request.positionChange?.let {
            settings.positionChange = it
            if (it) settings.teamManageSection = true
        }
        if (isManager) {
            request.memberChange?.let {
                settings.memberChange = it
                if (it) settings.teamManageSection = true
            }
        }

        if (settings.activitySection || settings.collaborationSection || settings.chatMessage || settings.teamManageSection) {
            settings.allNotification = true
        }

        val saved = settingRepository.save(settings)
        return NotificationSettingsResponse.of(saved, isManager)
    }

    @Transactional(readOnly = true)
    fun filterEnabledUserIds(userIds: List<Long>, message: NotificationMessage): List<Long> {
        if (userIds.isEmpty()) return emptyList()

        val settingsList = settingRepository.findAllByUserIdIn(userIds)
        val settingsMap = settingsList.associateBy { it.userId }

        return userIds.filter { userId ->
            val settings = settingsMap[userId] ?: return@filter true
            isNotificationEnabled(settings, message)
        }
    }

    fun isNotificationEnabled(settings: UserNotificationSettings, message: NotificationMessage): Boolean {
        return settings.isNotificationEnabled(message)
    }

    private fun getOrCreateSettings(userId: Long): UserNotificationSettings {
        return settingRepository.findByUserId(userId) ?: settingRepository.save(
            UserNotificationSettings(userId = userId)
        )
    }

    private fun checkIsManager(userId: Long): Boolean {
        val currentTeamId = userService.getCurrentTeamIdOrNull(userId) ?: return false
        val membership = teamMembershipRepository.findByTeamTeamIdAndUserIdAndLeftAtIsNull(currentTeamId, userId)
        return membership?.teamRole == TeamRole.LEADER || membership?.teamRole == TeamRole.MANAGER
    }
}

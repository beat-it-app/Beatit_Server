package com.beat_it.notification

import com.beat_it.notification.dto.NotificationSettingsResponse
import com.beat_it.notification.entity.UserNotificationSettings
import com.beat_it.notification.template.NotificationTemplate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NotificationSettingTest {

    @Test
    fun `기본 설정에서는 모든 알림이 허용된다`() {
        val settings = UserNotificationSettings(userId = 1L)
        val noticeMessage = NotificationTemplate.noticeRegistered("팀", "공지", "작성자", 1L)

        assertTrue(settings.isNotificationEnabled(noticeMessage))
    }

    @Test
    fun `마스터 전체 알림이 꺼지면 모든 알림이 차단된다`() {
        val settings = UserNotificationSettings(userId = 1L)
        settings.allNotification = false

        val noticeMessage = NotificationTemplate.noticeRegistered("팀", "공지", "작성자", 1L)
        val scheduleMessage = NotificationTemplate.scheduleRegistered("팀", "합주", 1L)

        assertFalse(settings.isNotificationEnabled(noticeMessage))
        assertFalse(settings.isNotificationEnabled(scheduleMessage))
    }

    @Test
    fun `캘린더 일정 알림이 꺼지면 일정 알림만 차단되고 공지는 허용된다`() {
        val settings = UserNotificationSettings(userId = 1L)
        settings.calendarSchedule = false

        val scheduleMessage = NotificationTemplate.scheduleRegistered("팀", "합주", 1L)
        val reminderMessage = NotificationTemplate.scheduleReminderD1("팀", "합주", 1L)
        val noticeMessage = NotificationTemplate.noticeRegistered("팀", "공지", "작성자", 1L)

        assertFalse(settings.isNotificationEnabled(scheduleMessage))
        assertFalse(settings.isNotificationEnabled(reminderMessage))
        assertTrue(settings.isNotificationEnabled(noticeMessage))
    }

    @Test
    fun `댓글 및 멘션이 아카이브와 공지투표별로 독립적으로 필터링된다`() {
        val settings = UserNotificationSettings(userId = 1L)
        // 아카이브 댓글은 끄고, 공지/투표 댓글은 켠 상태
        settings.activityCommentMention = false
        settings.collaborationCommentMention = true

        val archiveComment = NotificationTemplate.commentCreated("팀", "아카이브", 10L, directTo = "/archives/10")
        val noticeComment = NotificationTemplate.commentCreated("팀", "공지", 20L, directTo = "/posts/notices/20")

        // 아카이브 댓글은 activityCommentMention에 의해 차단
        assertFalse(settings.isNotificationEnabled(archiveComment))
        // 공지 댓글은 collaborationCommentMention에 의해 허용
        assertTrue(settings.isNotificationEnabled(noticeComment))
    }

    @Test
    fun `일반 멤버의 경우 응답에서 memberChange가 null로 반환된다`() {
        val settings = UserNotificationSettings(userId = 1L)

        val managerResponse = NotificationSettingsResponse.of(settings, isManager = true)
        val memberResponse = NotificationSettingsResponse.of(settings, isManager = false)

        assertTrue(managerResponse.isManager)
        assertNotNull(managerResponse.memberChange)

        assertFalse(memberResponse.isManager)
        assertNull(memberResponse.memberChange)
    }

    @Test
    fun `대분류 섹션이 꺼지면 하위 항목도 모두 차단된다`() {
        val settings = UserNotificationSettings(userId = 1L)
        settings.collaborationSection = false

        val noticeMessage = NotificationTemplate.noticeRegistered("팀", "공지", "작성자", 1L)
        val pollMessage = NotificationTemplate.pollCreated("팀", "투표", 1L)

        assertFalse(settings.isNotificationEnabled(noticeMessage))
        assertFalse(settings.isNotificationEnabled(pollMessage))
    }
}

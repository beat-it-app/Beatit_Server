package com.beat_it.notification

import com.beat_it.notification.dto.NotificationItemResponse
import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.template.NotificationCategory
import com.beat_it.notification.template.NotificationTemplate
import com.beat_it.notification.template.NotificationType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class NotificationTemplateTest {

    @Test
    fun `팀 멤버 가입 알림 템플릿 검증`() {
        val message = NotificationTemplate.memberJoin("알사탕 밴드", "홍길동")

        assertEquals(NotificationType.MEMBER_JOIN, message.type)
        assertEquals(NotificationCategory.TEAM, message.category)
        assertEquals("알사탕 밴드 멤버 가입", message.title)
        assertEquals("[알사탕 밴드] 홍길동님이 새로 합류했습니다.", message.content)
        assertEquals("/teams/members", message.directTo)
        assertTrue(message.isPush)
        assertTrue(message.isToast)
    }

    @Test
    fun `공지사항 등록 알림 템플릿 검증`() {
        val message = NotificationTemplate.noticeRegistered(
            teamName = "알사탕 밴드",
            noticeTitle = "합주 공지",
            authorName = "영서",
            noticeId = 10L
        )

        assertEquals(NotificationType.NOTICE_REGISTERED, message.type)
        assertEquals("알사탕 밴드 공지사항 등록", message.title)
        assertEquals("영서님이 공지사항을 등록했습니다.", message.content)
        assertEquals("[알사탕 밴드] 새 공지사항이 등록되었습니다: 합주 공지", message.pushText)
        assertEquals("/posts/notices/10", message.directTo)
        assertEquals(10L, message.targetId)
    }

    @Test
    fun `게시글 좋아요 알림 템플릿 검증`() {
        val message = NotificationTemplate.postLiked(
            teamName = "알사탕 밴드",
            likerName = "권우혁",
            postTitle = "[중요]모임 안내 공지",
            postId = 20L
        )

        assertEquals(NotificationType.POST_LIKED, message.type)
        assertEquals("게시글 좋아요!", message.title)
        assertEquals("권우혁님이 '[중요]모임 안내 공지'에 좋아요를 눌렀습니다.", message.content)
        assertEquals("/posts/20", message.directTo)
    }

    @Test
    fun `채팅 알림 템플릿 검증 - 토스트 false`() {
        val message = NotificationTemplate.chatMessage(
            teamName = "알사탕 밴드",
            chatRoomName = "보컬 파트방",
            senderName = "이기주",
            message = "안녕하세요!",
            chatRoomId = 5L
        )

        assertEquals(NotificationType.CHAT_MESSAGE, message.type)
        assertEquals(NotificationCategory.CHAT, message.category)
        assertEquals("[알사탕 밴드 / 보컬 파트방] 이기주: 안녕하세요!", message.content)
        assertTrue(message.isPush)
        assertFalse(message.isToast)
    }

    @Test
    fun `알림 시간 포맷팅 검증`() {
        val now = OffsetDateTime.now()
        val notification1 = Notifications(
            userId = 1L,
            teamId = 1L,
            notificationType = NotificationType.NOTICE_REGISTERED,
            category = NotificationCategory.NOTICE,
            title = "공지 등록",
            content = "공지가 등록되었습니다.",
            pushText = "공지가 등록되었습니다."
        )

        val itemResponse = NotificationItemResponse.from(notification1, now)
        assertEquals("방금 전", itemResponse.relativeTime)
    }
}

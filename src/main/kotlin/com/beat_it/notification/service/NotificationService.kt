package com.beat_it.notification.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.notification.dto.*
import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.repository.NotificationRepository
import com.beat_it.notification.template.NotificationMessage
import com.beat_it.notification.template.NotificationTemplate
import com.beat_it.team.service.TeamService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import com.beat_it.notification.kafka.NotificationProducer
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val teamService: TeamService,
    private val userService: UserService,
    private val pushNotificationService: PushNotificationService,
    private val notificationProducer: NotificationProducer
) {

    @Transactional(readOnly = true)
    fun getNotifications(userId: Long, page: Int = 0, size: Int = 20
    ): NotificationPageResponse {
        val currentTeamId = userService.getCurrentTeamId(userId)

        teamService.validateTeamMember(currentTeamId, userId)
        val teamName = teamService.getTeamName(currentTeamId)

        val pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val notificationPage = notificationRepository.findByUserIdAndTeamId(userId, currentTeamId, pageRequest)
        val unreadCount = notificationRepository.countByUserIdAndTeamIdAndIsReadFalse(userId, currentTeamId)

        return NotificationPageResponse(
            teamId = currentTeamId,
            teamName = teamName,
            unreadCount = unreadCount,
            hasUnread = unreadCount > 0,
            page = notificationPage.number,
            size = notificationPage.size,
            totalElements = notificationPage.totalElements,
            totalPages = notificationPage.totalPages,
            isLast = notificationPage.isLast,
            notifications = notificationPage.content.map { NotificationItemResponse.from(it) }
        )
    }

    @Transactional
    fun markAsRead(userId: Long, notificationId: Long): NotificationClickResponse {
        val notification = getNotificationWithPermission(notificationId, userId)

        notification.markAsRead()

        val teamName = teamService.getTeamName(notification.teamId)

        return NotificationClickResponse(
            notificationId = notification.notificationId ?: 0L,
            targetTeamId = notification.teamId,
            targetTeamName = teamName,
            directTo = notification.directTo,
            targetId = notification.targetId,
            isRead = notification.isRead
        )
    }

    @Transactional
    fun clickNotification(userId: Long, notificationId: Long): NotificationClickResponse {
        val notification = getNotificationWithPermission(notificationId, userId)

        val currentTeamId = userService.getCurrentTeamIdOrNull(userId)
        if (currentTeamId != notification.teamId) {
            teamService.validateTeamMember(notification.teamId, userId)
            userService.updateCurrentTeamId(userId, notification.teamId)
        }

        return markAsRead(userId, notificationId)
    }

    private fun getNotificationWithPermission(notificationId: Long, userId: Long): Notifications {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND) }

        if (notification.userId != userId) {
            throw BusinessException(ErrorCode.NOTIFICATION_NO_PERMISSION)
        }
        return notification
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        val currentTeamId = userService.getCurrentTeamId(userId)
        notificationRepository.markAllAsReadByUserIdAndTeamId(userId, currentTeamId)
    }

    @Transactional(readOnly = true)
    fun getUnreadStatus(userId: Long): NotificationUnreadStatusResponse {
        val currentTeamId = userService.getCurrentTeamId(userId)
        val unreadCount = notificationRepository.countByUserIdAndTeamIdAndIsReadFalse(userId, currentTeamId)

        return NotificationUnreadStatusResponse(
            teamId = currentTeamId,
            unreadCount = unreadCount,
            hasUnread = unreadCount > 0
        )
    }

    @Transactional
    fun sendNotification(userId: Long, teamId: Long, message: NotificationMessage
    ): Notifications {
        val notification = Notifications(
            userId = userId,
            teamId = teamId,
            notificationType = message.type,
            category = message.category,
            title = message.title,
            content = message.content,
            pushText = message.pushText,
            directTo = message.directTo,
            targetId = message.targetId,
            isPush = message.isPush,
            isToast = message.isToast,
            isRead = false
        )
        val savedNotification = notificationRepository.save(notification)
        if (savedNotification.isPush) {
            pushNotificationService.sendPush(savedNotification)
        }
        return savedNotification
    }

    @Transactional
    fun sendNotifications(userIds: List<Long>, teamId: Long, message: NotificationMessage
    ): List<Notifications> {
        val distinctUserIds = userIds.distinct()
        if (distinctUserIds.isEmpty()) return emptyList()

        val notifications = distinctUserIds.map { targetUserId ->
            Notifications(
                userId = targetUserId,
                teamId = teamId,
                notificationType = message.type,
                category = message.category,
                title = message.title,
                content = message.content,
                pushText = message.pushText,
                directTo = message.directTo,
                targetId = message.targetId,
                isPush = message.isPush,
                isToast = message.isToast,
                isRead = false
            )
        }
        val savedNotifications = notificationRepository.saveAll(notifications)
        pushNotificationService.sendPushes(savedNotifications)
        return savedNotifications
    }

    // 테스트용: 카프카를 통한 샘플 알림 4개 발행
    fun createMockNotifications(targetUserId: Long, targetTeamId: Long): String {
        val teamName = teamService.getTeamName(targetTeamId)

        val mockMessages = listOf(
            // 1번 알림: 공지사항 등록 / 영서님이 공지사항을 등록했습니다.
            NotificationTemplate.noticeRegistered(
                teamName = teamName,
                noticeTitle = "합주 공지",
                authorName = "영서",
                noticeId = 1L
            ),
            // 2번 알림: 게시글 좋아요! / 권우혁님이 '[중요]모임 안내 공지'에 좋아요를 눌렀습니다.
            NotificationTemplate.postLiked(
                teamName = teamName,
                likerName = "권우혁",
                postTitle = "[중요]모임 안내 공지",
                postId = 1L
            ),
            // 3번 알림: 일정 알림 / 내일은 '정기 합주'가 있는 날입니다!
            NotificationTemplate.scheduleReminderD1(
                teamName = teamName,
                scheduleTitle = "정기 합주",
                scheduleId = 1L
            ),
            // 4번 알림: 새로운 밋잇 생성 / 새로운 밋잇(시간 조율)이 열렸습니다. 가능한 일정을 등록해 보세요!
            NotificationTemplate.meetitCreated(
                teamName = teamName,
                meetitTitle = "시간 조율",
                meetitId = 1L
            )
        )

        mockMessages.forEach { message ->
            notificationProducer.sendNotification(
                targetUserId = targetUserId,
                teamId = targetTeamId,
                message = message
            )
        }

        return "샘플 알림 4개가 카프카 토픽으로 발행되었습니다."
    }
}

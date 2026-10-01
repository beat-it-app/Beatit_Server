package com.beat_it.notification.service

import com.beat_it.auth.service.UserService
import com.beat_it.global.error.BusinessException
import com.beat_it.global.error.ErrorCode
import com.beat_it.notification.dto.*
import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.repository.NotificationRepository
import com.beat_it.notification.template.NotificationMessage
import com.beat_it.team.service.TeamService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val teamService: TeamService,
    private val userService: UserService
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

    /**
     * 알림 클릭 및 단건 읽음 처리:
     * 1. 알림 조회 및 본인 소유 확인
     * 2. 해당 알림의 팀 멤버인지 검증 및 활성 팀(currentTeamId) 자동 전환
     * 3. 알림 읽음 처리 (isRead = true)
     * 4. direct to 이동 정보(directTo, targetTeamId, targetId 등) 반환
     */
    @Transactional
    fun markAsRead(userId: Long, notificationId: Long): NotificationClickResponse {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND) }

        if (notification.userId != userId) {
            throw BusinessException(ErrorCode.NOTIFICATION_NO_PERMISSION)
        }

        // 해당 팀 멤버인지 검증 후 활성 팀 전환
        teamService.validateTeamMember(notification.teamId, userId)
        val teamName = teamService.getTeamName(notification.teamId)
        userService.updateCurrentTeamId(userId, notification.teamId)

        // 읽음 처리
        notification.markAsRead()

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
        return notificationRepository.save(notification)
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
        return notificationRepository.saveAll(notifications)
    }
}

package com.beat_it.notification.event

import com.beat_it.notification.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class NotificationEventListener(
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async("notificationAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun handleNotificationEvent(event: NotificationEvent) {
        val targetUserIds = event.targetUserIds.distinct()
        if (targetUserIds.isEmpty()) {
            log.debug("NotificationEvent ignored: targetUserIds is empty. teamId={}", event.teamId)
            return
        }

        try {
            log.info(
                "Handling NotificationEvent: type={}, teamId={}, targetCount={}",
                event.message.type,
                event.teamId,
                targetUserIds.size
            )
            notificationService.sendNotifications(
                userIds = targetUserIds,
                teamId = event.teamId,
                message = event.message
            )
        } catch (e: Exception) {
            log.error("Failed to process NotificationEvent for teamId={}: {}", event.teamId, e.message, e)
        }
    }
}

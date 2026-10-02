package com.beat_it.notification.service

import com.beat_it.notification.entity.NotificationDeliveryLogs
import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.entity.enum.DeliveryChannel
import com.beat_it.notification.entity.enum.DeliveryStatus
import com.beat_it.notification.repository.NotificationDeliveryLogRepository
import com.beat_it.notification.repository.PushTokenRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushNotificationService(
    private val pushTokenRepository: PushTokenRepository,
    private val deliveryLogRepository: NotificationDeliveryLogRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun sendPush(notification: Notifications) {
        val notificationId = notification.notificationId ?: return
        val activeTokens = pushTokenRepository.findAllByUserIdAndIsActiveTrue(notification.userId)

        if (activeTokens.isEmpty()) {
            log.info("푸시 발송 건너뜀 (활성 푸시 토큰 없음) - userId: {}", notification.userId)
            return
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp이 초기화되지 않아 FCM 푸시 발송을 건너뜁니다.")
            return
        }

        activeTokens.forEach { token ->
            val deliveryLog = NotificationDeliveryLogs(
                notificationId = notificationId,
                deliveryChannel = DeliveryChannel.PUSH,
                deliveryStatus = DeliveryStatus.PENDING
            )
            val savedLog = deliveryLogRepository.save(deliveryLog)

            try {
                val fcmMessage = Message.builder()
                    .setToken(token.pushToken)
                    .setNotification(
                        Notification.builder()
                            .setTitle(notification.title)
                            .setBody(notification.pushText)
                            .build()
                    )
                    .putData("notificationId", notificationId.toString())
                    .putData("teamId", notification.teamId.toString())
                    .putData("directTo", notification.directTo ?: "")
                    .putData("targetId", notification.targetId?.toString() ?: "")
                    .putData("category", notification.category.name)
                    .putData("type", notification.notificationType.name)
                    .build()

                val response = FirebaseMessaging.getInstance().send(fcmMessage)
                log.info("FCM 푸시 발송 성공 - messageId: {}, token: {}", response, token.pushToken.take(15) + "...")

                token.markAsUsed()
                savedLog.markSuccess()
            } catch (e: FirebaseMessagingException) {
                log.error("FCM 푸시 발송 실패 (Firebase 에러) - code: {}, msg: {}", e.messagingErrorCode, e.message)
                savedLog.markFailed(e.message)

                if (e.messagingErrorCode == MessagingErrorCode.UNREGISTERED ||
                    e.messagingErrorCode == MessagingErrorCode.INVALID_ARGUMENT
                ) {
                    log.warn("유효하지 않은 FCM 토큰 감지 -> 비활성화 처리: {}", token.pushToken.take(15) + "...")
                    token.deactivate()
                }
            } catch (e: Exception) {
                log.error("FCM 푸시 발송 중 예외 발생 - notificationId: {}", notificationId, e)
                savedLog.markFailed(e.message)
            }
        }
    }

    @Transactional
    fun sendPushes(notifications: List<Notifications>) {
        notifications.forEach { notification ->
            if (notification.isPush) {
                sendPush(notification)
            }
        }
    }
}

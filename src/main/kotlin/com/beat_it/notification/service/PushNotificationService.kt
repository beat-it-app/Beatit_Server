package com.beat_it.notification.service

import com.beat_it.notification.entity.NotificationDeliveryLogs
import com.beat_it.notification.entity.Notifications
import com.beat_it.notification.entity.enum.DeliveryChannel
import com.beat_it.notification.entity.enum.DeliveryStatus
import com.beat_it.notification.repository.NotificationDeliveryLogRepository
import com.beat_it.notification.repository.PushTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushNotificationService(
    private val pushTokenRepository: PushTokenRepository,
    private val deliveryLogRepository: NotificationDeliveryLogRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 알림에 대한 푸시 발송 및 발송 로그 기록
     * 비동기 처리 또는 이벤트 기반으로 확장 가능합니다.
     */
    @Transactional
    fun sendPush(notification: Notifications) {
        val notificationId = notification.notificationId ?: return
        val activeTokens = pushTokenRepository.findAllByUserIdAndIsActiveTrue(notification.userId)

        if (activeTokens.isEmpty()) {
            log.info("푸시 발송 건너뜀 (활성 푸시 토큰 없음) - userId: {}", notification.userId)
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
                // TODO: FCM 연동 시 FirebaseMessaging.getInstance().send(message) 호출
                // 현재는 FCM 키 연동 전 시뮬레이션 및 발송 로그 기록 지원
                log.info(
                    "FCM 푸시 발송 시도 - targetToken: {}, title: {}, content: {}, directTo: {}",
                    token.pushToken.take(15) + "...",
                    notification.title,
                    notification.pushText,
                    notification.directTo
                )

                token.markAsUsed()
                savedLog.markSuccess()
            } catch (e: Exception) {
                log.error("FCM 푸시 발송 실패 - notificationId: {}, token: {}", notificationId, token.pushToken, e)
                savedLog.markFailed(e.message)
            }
        }
    }

    /**
     * 다수 사용자 대상 푸시 발송 및 발송 로그 기록
     */
    @Transactional
    fun sendPushes(notifications: List<Notifications>) {
        notifications.forEach { notification ->
            if (notification.isPush) {
                sendPush(notification)
            }
        }
    }
}

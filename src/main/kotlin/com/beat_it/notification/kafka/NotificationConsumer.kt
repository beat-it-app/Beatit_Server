package com.beat_it.notification.kafka

import com.beat_it.notification.dto.NotificationKafkaDto
import com.beat_it.notification.service.NotificationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationConsumer(
    private val objectMapper: ObjectMapper,
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [NotificationProducer.NOTIFICATION_TOPIC], groupId = "notification-group")
    fun consume(message: String) {
        log.info("[NotificationConsumer] 알림 메시지 수신 완료")
        try {
            val dto = objectMapper.readValue(message, NotificationKafkaDto::class.java)
            val targetUserIds = dto.targetUserIds.distinct()
            if (targetUserIds.isEmpty()) return

            notificationService.sendNotifications(
                userIds = targetUserIds,
                teamId = dto.teamId,
                message = dto.message
            )
            log.info("[NotificationConsumer] 알림 DB 저장 및 발송 완료 - type: {}, count: {}", dto.message.type, targetUserIds.size)
        } catch (e: Exception) {
            log.error("[NotificationConsumer] 알림 처리 중 에러 발생: {}", e.message, e)
        }
    }
}

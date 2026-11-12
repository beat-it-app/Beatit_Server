package com.beat_it.notification.kafka

import com.beat_it.notification.dto.NotificationKafkaDto
import com.beat_it.notification.template.NotificationMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class NotificationProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val NOTIFICATION_TOPIC = "notification-topic"
    }

    fun sendNotification(dto: NotificationKafkaDto) {
        try {
            val json = objectMapper.writeValueAsString(dto)
            log.info("[NotificationProducer] 토픽($NOTIFICATION_TOPIC)으로 알림 발행: type={}, targetCount={}", dto.message.type, dto.targetUserIds.size)
            kafkaTemplate.send(NOTIFICATION_TOPIC, json)
        } catch (e: Exception) {
            log.error("[NotificationProducer] 카프카 브로커 연결 또는 알림 발행 실패 (카프카 서버가 켜져 있는지 확인하세요): {}", e.message, e)
            throw com.beat_it.global.error.BusinessException(com.beat_it.global.error.ErrorCode.KAFKA_SERVER_UNAVAILABLE)
        }
    }

    fun sendNotification(targetUserIds: List<Long>, teamId: Long, message: NotificationMessage) {
        sendNotification(NotificationKafkaDto(targetUserIds, teamId, message))
    }

    fun sendNotification(targetUserId: Long, teamId: Long, message: NotificationMessage) {
        sendNotification(NotificationKafkaDto(targetUserId, teamId, message))
    }
}

package com.beat_it.chat.event

import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ChatRoomEventListener(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val redisTemplate: StringRedisTemplate
) {

    @EventListener
    fun handleChatRoomCreated(event: ChatRoomCreatedEvent) {
        println("🔔 [Event Listener] 채팅방 생성 이벤트 감지: roomId = ${event.chatId}")

        val redisKey = "chatroom:${event.chatId}:members"

        try {
            event.participantIds.forEach { userId ->
                redisTemplate.opsForSet().add(redisKey, userId.toString())
            }
            println("[Redis] 채팅방 [${event.chatId}] 참여자 명단 캐싱 완료")
        } catch (e: Exception) {
            println("[Redis] 캐싱 실패: ${e.message}")
        }

        val kafkaMessage = """
            {
                "chatId": ${event.chatId},
                "roomName": "${event.roomName}",
                "action": "CREATED"
            }
        """.trimIndent()

        try {
            kafkaTemplate.send("room-lifecycle-topic", kafkaMessage)
            println("[Kafka] room-lifecycle-topic 으로 방 생성 알림 발행 성공")
        } catch (e: Exception) {
            println("[Kafka] 알림 발행 실패: ${e.message}")
        }
    }
}
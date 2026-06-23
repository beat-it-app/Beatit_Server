package com.beat_it.chat.event

import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ChatRoomEventListener( // 👈 지원님이 정하신 정석 네이밍 그대로 유지!
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val redisTemplate: StringRedisTemplate // Redis 파싱 버그를 방지하기 위해 String 전용 템플릿 사용
) {

    @EventListener
    fun handleChatRoomCreated(event: ChatRoomCreatedEvent) {
        println("🔔 [Event Listener] 채팅방 생성 이벤트 감지: roomId = ${event.chatId}")

        // 1. Redis 연동: 방이 만들어졌으니 참여자 명단을 Redis Set 구조에 캐싱
        // Key 예시: "chatroom:1:members" -> [ "1", "2", "3" ]
        val redisKey = "chatroom:${event.chatId}:members"

        try {
            event.participantIds.forEach { userId ->
                redisTemplate.opsForSet().add(redisKey, userId.toString())
            }
            println("[Redis] 채팅방 [${event.chatId}] 참여자 명단 캐싱 완료")
        } catch (e: Exception) {
            println("[Redis] 캐싱 실패: ${e.message}")
        }

        // 2. Kafka 연동: 다른 멀티 서버 인스턴스들에게 새 방이 생겼음을 브로드캐스팅
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
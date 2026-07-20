package com.beat_it.chat.kafka

import com.beat_it.chat.dto.ChatMessageDetailResponse
import com.beat_it.chat.entity.ChatMessage
import com.beat_it.chat.entity.ChatMessageType
import com.beat_it.chat.handler.ChatWebSocketHandler
import com.beat_it.chat.repository.ChatMessageRepository
import com.beat_it.chat.repository.ChatRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage

@Component
class ChatConsumer(
    private val objectMapper: ObjectMapper,
) {

    @KafkaListener(topics = ["chat-topic"], groupId = "chat-group")
    fun consume(message: String) {
        println("[Kafka Consumer] 카프카 메시지 수신 완료")

        try {
            val chatData = objectMapper.readValue(message, ChatMessageDetailResponse::class.java)
            val targetChatId = chatData.chatId

            val activeSessions = ChatWebSocketHandler.roomSessions[targetChatId]

            if (activeSessions != null && activeSessions.isNotEmpty()) {
                activeSessions.values.forEach { session ->
                    if (session.isOpen) {
                        session.sendMessage(TextMessage(message))
                    }
                }
                println("[Kafka Consumer] 채팅방 [$targetChatId]의 유저 ${activeSessions.size}명에게 실시간 동기화 완료")
            } else {
                println("[Kafka Consumer] 채팅방 [$targetChatId]에 현재 접속 중인 실시간 웹소켓 세션이 없습니다.")
            }

        } catch (e: Exception) {
            println("[Kafka Consumer] 브로드캐스팅 분기 처리 실패: ${e.message}")
        }
    }
}
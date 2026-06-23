package com.beat_it.chat.kafka

import com.beat_it.chat.handler.ChatWebSocketHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage

@Component
class ChatConsumer {

    @KafkaListener(topics = ["chat-topic"], groupId = "chat-group")
    fun consume(message: String) {
        println("[Kafka Consumer] 카프카 토픽에서 메시지 수신: $message")

        ChatWebSocketHandler.sessions.values.forEach { session ->
            if (session.isOpen) {
                session.sendMessage(TextMessage("서버(Kafka 경유)가 전달함: $message"))
            }
        }
    }
}
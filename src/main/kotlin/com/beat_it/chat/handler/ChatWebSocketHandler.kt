package com.beat_it.chat.handler

import com.beat_it.chat.dto.ChatMessageDetailResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    companion object {
        val roomSessions = ConcurrentHashMap<Long, ConcurrentHashMap<String, WebSocketSession>>()
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val path = session.uri?.path
        val chatId = path?.substringAfterLast("/")?.toLongOrNull()

        if (chatId != null) {
            roomSessions.computeIfAbsent(chatId) { ConcurrentHashMap() }[session.id] = session
            println("[WS] 채팅방 [chatId: $chatId]에 새로운 세션 편입 성공 (ID: ${session.id})")
        } else {
            println("[WS] URL에 올바른 chatId가 없어 임시 세션으로 처리합니다. (ID: ${session.id})")
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        println("[WS] 클라이언트 실시간 다이렉트 데이터 수신: $payload")

        try {
            val chatData = objectMapper.readValue(payload, ChatMessageDetailResponse::class.java)
            val chatId = chatData.chatId

            kafkaTemplate.send("chat-topic", payload)
            println("[WS ➔ Kafka] 채팅방 [$chatId] 메시지 카프카 토픽 발송 완료")

        } catch (e: Exception) {
            println("[WS Handler] 카프카 전송 실패 또는 JSON 파싱 에러: ${e.message}")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        roomSessions.values.forEach { sessions ->
            sessions.remove(session.id)
        }
        println("[WS] 웹소켓 연결 종료 (ID: ${session.id})")
    }
}
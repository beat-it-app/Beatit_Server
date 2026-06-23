package com.beat_it.chat.handler

import com.beat_it.chat.kafka.ChatProducer // 👈 1. 우리가 방금 만든 Producer 임포트
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val chatProducer: ChatProducer // 👈 2. 여기에 카프카 Producer를 주입받습니다.
) : TextWebSocketHandler() {

    // 다른 곳(Consumer)에서도 유저 세션 목록에 접근할 수 있도록 companion object로 공유합니다.
    companion object {
        val sessions = ConcurrentHashMap<String, WebSocketSession>()
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
        println("새로운 웹소켓 연결 성공! 세션 ID: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        println("[WS Handler] 클라이언트에게 메시지 수신: $payload")

        chatProducer.sendMessage("chat-topic", payload)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
        println("웹소켓 연결 종료: ${session.id}")
    }
}
package com.beat_it.chat.dto

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

data class ChatRoomCreateResponse(
    val chatId: Long,
    val roomName: String,
    val createdAt: String
) {
    companion object {
        fun of(chatId: Long, roomName: String, createdAt: OffsetDateTime): ChatRoomCreateResponse {
            return ChatRoomCreateResponse(
                chatId = chatId,
                roomName = roomName,
                createdAt = createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            )
        }
    }
}
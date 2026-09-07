package com.beat_it.chat.dto

import java.time.OffsetDateTime

data class ChatMessageDetailResponse(
    val messageId: Long,
    val chatId: Long,
    val senderId: Long,
    val content: String,
    val messageType: String,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun of(
            messageId: Long,
            chatId: Long,
            senderId: Long,
            content: String,
            messageType: String,
            createdAt: OffsetDateTime
        ): ChatMessageDetailResponse {
            return ChatMessageDetailResponse(
                messageId = messageId,
                chatId = chatId,
                senderId = senderId,
                content = content,
                messageType = messageType,
                createdAt = createdAt
            )
        }
    }
}
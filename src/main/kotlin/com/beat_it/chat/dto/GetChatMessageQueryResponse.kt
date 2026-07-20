package com.beat_it.chat.dto

data class GetChatMessageQueryResponse(
    val messageId: Long,
    val senderId: Long,
    val senderName: String,
    val profileImageUrl: String?,
    val content: String,
    val messageType: String,
    val createdAt: String,
    val isMine: Boolean
) {
    companion object {
        fun of(
            messageId: Long,
            senderId: Long,
            senderName: String,
            profileImageUrl: String?,
            content: String,
            messageType: String,
            createdAt: String,
            isMine: Boolean
        ): GetChatMessageQueryResponse {
            return GetChatMessageQueryResponse(
                messageId = messageId,
                senderId = senderId,
                senderName = senderName,
                profileImageUrl = profileImageUrl,
                content = content,
                messageType = messageType,
                createdAt = createdAt,
                isMine = isMine
            )
        }
    }
}
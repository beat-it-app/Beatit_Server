package com.beat_it.chat.dto

import com.beat_it.auth.dto.UserProfileResponse
import com.beat_it.chat.entity.ChatMessage
import java.time.OffsetDateTime

data class GetChatMessageQueryResponse(
    val messageId: Long,
    val senderId: Long,
    val senderName: String,
    val profileImageUrl: String?,
    val content: String,
    val messageType: String,
    val createdAt: OffsetDateTime,
    val isMine: Boolean,
    val readByUsers: List<ChatMessageReadUserResponse> = emptyList()
) {
    companion object {
        fun of(
            message: ChatMessage,
            profile: UserProfileResponse?,
            currentUserId: Long,
            readByUsers: List<ChatMessageReadUserResponse> = emptyList()
        ): GetChatMessageQueryResponse {
            val isMine = (message.senderId == currentUserId)
            return GetChatMessageQueryResponse(
                messageId = message.chatMessageId!!,
                senderId = message.senderId,
                senderName = profile?.name ?: "알 수 없는 사용자",
                profileImageUrl = if (isMine) null else profile?.profileImageUrl,
                content = message.content,
                messageType = message.type.name,
                createdAt = message.createdAt,
                isMine = isMine,
                readByUsers = readByUsers
            )
        }
    }
}
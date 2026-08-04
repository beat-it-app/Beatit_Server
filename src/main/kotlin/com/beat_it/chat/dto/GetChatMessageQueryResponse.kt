package com.beat_it.chat.dto

import com.beat_it.auth.dto.UserProfileResponse
import com.beat_it.chat.entity.ChatMessage
import com.beat_it.global.util.DateTimeUtil

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
            message: ChatMessage,
            profile: UserProfileResponse?,
            currentUserId: Long,
        ): GetChatMessageQueryResponse {
            return GetChatMessageQueryResponse(
                messageId = message.chatMessageId!!,
                senderId = message.senderId,
                senderName = profile?.name ?: "알 수 없는 사용자",
                profileImageUrl = profile?.profileImageUrl,
                content = message.content,
                messageType = message.type.name,
                createdAt = DateTimeUtil.format(message.createdAt),
                isMine = (message.senderId == currentUserId)
            )
        }
    }
}
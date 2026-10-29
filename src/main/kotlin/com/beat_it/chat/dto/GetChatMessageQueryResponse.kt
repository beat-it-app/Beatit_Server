package com.beat_it.chat.dto

import com.beat_it.auth.dto.UserProfileResponse
import com.beat_it.chat.entity.ChatMessage
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import com.beat_it.chat.dto.ChatMessageReadUserResponse

data class GetChatMessageQueryResponse(
    val messageId: Long,
    val senderId: Long,
    val senderName: String,
    val profileImageUrl: String?,
    val content: String,
    val messageType: String,
    val fileSizeBytes: Long? = null,
    val fileSizeDisplay: String? = null,
    val createdAt: OffsetDateTime,
    @get:JsonProperty("isMine")
    val isMine: Boolean,
    val readByUsers: List<ChatMessageReadUserResponse> = emptyList()
) {
    companion object {
        fun of(
            message: ChatMessage,
            profile: UserProfileResponse?,
            currentUserId: Long,
            fileSizeBytes: Long? = null,
            fileSizeDisplay: String? = null,
            readByUsers: List<ChatMessageReadUserResponse> = emptyList()
        ): GetChatMessageQueryResponse {
            val isMine = (message.senderId == currentUserId)
            return GetChatMessageQueryResponse(
                messageId = message.chatMessageId!!,
                senderId = message.senderId,
                senderName = profile?.name ?: "알 수 없음",
                profileImageUrl = if (isMine) null else profile?.profileImageUrl,
                content = message.content,
                messageType = message.type.name,
                fileSizeBytes = fileSizeBytes,
                fileSizeDisplay = fileSizeDisplay,
                createdAt = message.createdAt,
                isMine = isMine,
                readByUsers = readByUsers
            )
        }
    }
}
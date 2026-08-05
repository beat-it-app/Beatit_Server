package com.beat_it.chat.dto

data class ChatRoomDetailResponse(
    val chatroomName: String,
    val participantCount: Int,
    val messages: List<GetChatMessageQueryResponse>,
    val hasNext: Boolean
) {
    companion object {
        fun of(
            chatroomName: String,
            participantCount: Int,
            messages: List<GetChatMessageQueryResponse>,
            hasNext: Boolean
        ): ChatRoomDetailResponse {
            return ChatRoomDetailResponse(chatroomName, participantCount, messages, hasNext)
        }
    }
}
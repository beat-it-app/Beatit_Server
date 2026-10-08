package com.beat_it.chat.event

data class ChatMessageReadEvent(
    val type: String = "READ",
    val chatId: Long,
    val userId: Long,
    val lastReadMessageId: Long
)
package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByChatRoomChatIdOrderByChatMessageIdDesc(chatId: Long, pageable: Pageable): Slice<ChatMessage>
    fun findTopByChatRoomChatIdOrderByChatMessageIdDesc(chatId: Long): ChatMessage?
}
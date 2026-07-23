package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatMessage
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByChatRoomChatIdOrderByChatMessageIdDesc(chatId: Long, pageable: Pageable): Slice<ChatMessage>
    fun findTopByChatRoomChatIdOrderByChatMessageIdDesc(chatId: Long): ChatMessage?

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.chatId = :chatId AND m.chatMessageId > :lastChatMessageId AND m.senderId != :currentUserId")
    fun countUnreadMessages(
        @Param("chatId") chatId: Long,
        @Param("lastChatMessageId") lastChatMessageId: Long,
        @Param("currentUserId") currentUserId: Long
    ): Long

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.chatId = :chatId AND m.senderId != :currentUserId")
    fun countAllUnreadMessages(
        @Param("chatId") chatId: Long,
        @Param("currentUserId") currentUserId: Long
    ): Long
}
package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatMessage
import com.beat_it.chat.entity.ChatMessageType
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByChatRoomChatIdOrderByChatMessageIdDesc(chatId: Long, pageable: Pageable): Slice<ChatMessage>

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom.chatId = :chatId AND m.chatMessageId > :lastChatMessageId AND m.senderId != :currentUserId")
    fun countUnreadMessages(
        @Param("chatId") chatId: Long,
        @Param("lastChatMessageId") lastChatMessageId: Long,
        @Param("currentUserId") currentUserId: Long
    ): Long

    @Query("""
        SELECT COUNT(m) FROM ChatMessage m 
        WHERE m.chatRoom.chatId = :chatId 
        AND m.senderId != :currentUserId 
        AND m.type != :systemType
    """)
    fun countAllUnreadMessages(
        @Param("chatId") chatId: Long,
        @Param("currentUserId") currentUserId: Long,
        @Param("systemType") systemType: ChatMessageType = ChatMessageType.SYSTEM
    ): Long

    fun findByChatRoomChatIdAndCreatedAtAfterOrderByChatMessageIdDesc(
        chatId: Long,
        leftAt: OffsetDateTime,
        pageable: Pageable
    ): Slice<ChatMessage>

    @Query("""
    SELECT m FROM ChatMessage m 
    WHERE m.chatMessageId IN (
        SELECT MAX(cm.chatMessageId) FROM ChatMessage cm 
        WHERE cm.chatRoom.chatId IN :chatIds 
        GROUP BY cm.chatRoom.chatId
    )
""")
    fun findTopMessagesByChatRoomChatIds(@Param("chatIds") chatIds: List<Long>): List<ChatMessage>
}
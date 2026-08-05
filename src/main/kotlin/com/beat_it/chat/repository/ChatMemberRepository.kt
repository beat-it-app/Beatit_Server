package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatMemberRepository : JpaRepository<ChatMember, Long> {

    @Query("SELECT COUNT(cm) > 0 FROM ChatMember cm WHERE cm.chatRoom.chatId = :chatId AND cm.userId = :userId")
    fun existsByChatRoomChatIdAndUserId(
        @Param("chatId") chatId: Long,
        @Param("userId") userId: Long
    ): Boolean

    fun findByChatRoomChatIdAndUserId(chatId: Long, userId: Long): ChatMember?

    @Query("SELECT COUNT(cm) FROM ChatMember cm WHERE cm.chatRoom.chatId = :chatId AND cm.isParticipating = true")
    fun countByChatRoomChatId(@Param("chatId") chatId: Long): Long

    @Query("SELECT cm FROM ChatMember cm JOIN FETCH cm.chatRoom WHERE cm.chatRoom.chatId = :chatId AND cm.userId = :userId AND cm.isParticipating = true")
    fun findByChatRoomChatIdAndUserIdWithChatRoom(
        @Param("chatId") chatId: Long,
        @Param("userId") userId: Long
    ): ChatMember?
}
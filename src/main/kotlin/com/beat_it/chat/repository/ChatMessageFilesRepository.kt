package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatMessageFiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageFilesRepository : JpaRepository<ChatMessageFiles, Long> {

    @Query("""
        SELECT cmf FROM ChatMessageFiles cmf
        JOIN FETCH cmf.chatFile
        WHERE cmf.chatMessage.chatMessageId IN :messageIds
    """)
    fun findAllByChatMessageChatMessageIdIn(
        @Param("messageIds") messageIds: List<Long>
    ): List<ChatMessageFiles>

    fun findByChatMessageChatMessageId(chatMessageId: Long): ChatMessageFiles?
}
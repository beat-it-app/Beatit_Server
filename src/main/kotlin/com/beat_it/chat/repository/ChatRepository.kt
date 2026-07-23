package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : JpaRepository<ChatRoom, Long> {
    @Query("SELECT DISTINCT r FROM ChatRoom r JOIN FETCH r.members m WHERE m.userId = :userId")
    fun findByMembersUserId(@Param("userId") userId: Long): List<ChatRoom>
}
package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : JpaRepository<ChatRoom, Long> {
    @Query("""
        SELECT DISTINCT c FROM ChatRoom c
        JOIN c.members m
        WHERE c.teamId = :teamId
          AND m.userId = :userId
          AND m.isParticipating = true
    """)
    fun findByTeamIdAndMembersUserIdAndIsParticipatingTrue(
        @Param("teamId") teamId: Long,
        @Param("userId") userId: Long
    ): List<ChatRoom>
}
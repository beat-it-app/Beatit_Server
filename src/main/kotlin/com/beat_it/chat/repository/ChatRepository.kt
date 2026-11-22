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

    @Query("""
    SELECT c FROM ChatRoom c
    JOIN c.members m1
    JOIN c.members m2
    WHERE c.teamId = :teamId
      AND c.type = com.beat_it.chat.entity.enum.ChatRoomType.DIRECT
      AND m1.userId = :userId1
      AND m2.userId = :userId2
""")
    fun findDirectChatRoom(
        @Param("teamId") teamId: Long,
        @Param("userId1") userId1: Long,
        @Param("userId2") userId2: Long
    ): ChatRoom?

    @Query("""
    SELECT c FROM ChatRoom c
    JOIN c.members m
    WHERE c.teamId = :teamId
      AND c.type = com.beat_it.chat.entity.enum.ChatRoomType.GROUP
      AND SIZE(c.members) = :memberCount
      AND m.userId IN :userIds
    GROUP BY c
    HAVING COUNT(DISTINCT m.userId) = :memberCount
""")
    fun findDuplicateGroupChatRooms(
        @Param("teamId") teamId: Long,
        @Param("userIds") userIds: List<Long>,
        @Param("memberCount") memberCount: Int
    ): List<ChatRoom>
}
package com.beat_it.chat.repository

import com.beat_it.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : JpaRepository<ChatRoom, Long> {

    /**
     * 특정 채팅방에 특정 유저가 참여하고 있는지 여부 확인
     * ChatRoom과 ChatMember의 조인을 통해 안전하게 검증합니다.
     */
    @Query("SELECT COUNT(cm) > 0 FROM ChatRoom cr JOIN cr.members cm WHERE cr.id = :chatRoomId AND cm.userId = :userId")
    fun existsByChatRoomIdAndUserId(
        @Param("chatRoomId") chatRoomId: Long,
        @Param("userId") userId: Long
    ): Boolean
}
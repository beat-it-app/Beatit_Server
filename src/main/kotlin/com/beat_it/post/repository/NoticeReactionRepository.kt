package com.beat_it.post.repository

import com.beat_it.post.entity.NoticeReactions
import com.beat_it.post.entity.enum.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface NoticeReactionRepository : JpaRepository<NoticeReactions, Long> {
    fun findByNoticeNoticeIdAndUserId(noticeId: Long, userId: Long): Optional<NoticeReactions>
    fun findByNoticeNoticeIdAndUserIdAndReactionType(noticeId: Long, userId: Long, reactionType: ReactionType): Optional<NoticeReactions>
    fun deleteByNoticeNoticeId(noticeId: Long)
}

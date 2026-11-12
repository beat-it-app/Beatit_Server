package com.beat_it.post.repository.notice

import com.beat_it.post.entity.notice.NoticeReactions
import com.beat_it.post.entity.enum.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface NoticeReactionRepository : JpaRepository<NoticeReactions, Long> {
    fun findByNoticeNoticeIdAndUserId(noticeId: Long, userId: Long): Optional<NoticeReactions>
    fun deleteByNoticeNoticeId(noticeId: Long)
}

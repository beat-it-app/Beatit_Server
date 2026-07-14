package com.beat_it.post.repository.notice

import com.beat_it.post.entity.notice.NoticeAttachments
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoticeAttachmentsRepository : JpaRepository<NoticeAttachments, Long> {
    fun findByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId: Long): List<NoticeAttachments>
    fun findFirstByNoticeNoticeIdOrderByDisplayOrderAsc(noticeId: Long): NoticeAttachments?
    fun deleteByNoticeNoticeId(noticeId: Long)
}

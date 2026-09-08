package com.beat_it.team.repository

import com.beat_it.team.entity.ArchiveCommentMentions
import com.beat_it.team.entity.ArchiveComments
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchiveCommentMentionRepository : JpaRepository<ArchiveCommentMentions, Long> {
    fun findByCommentIn(comments: List<ArchiveComments>): List<ArchiveCommentMentions>
    fun deleteByComment(comment: ArchiveComments)
    fun deleteByCommentArchiveCommentIdIn(commentIds: List<Long>)
}

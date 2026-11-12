package com.beat_it.post.repository

import com.beat_it.post.entity.PostCommentMentions
import com.beat_it.post.entity.PostComments
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostCommentMentionRepository : JpaRepository<PostCommentMentions, Long> {
    fun findByCommentIn(comments: List<PostComments>): List<PostCommentMentions>
    fun findByCommentCommentId(commentId: Long): List<PostCommentMentions>
    fun findByCommentCommentIdIn(commentIds: List<Long>): List<PostCommentMentions>
    fun deleteByComment(comment: PostComments)
    fun deleteByCommentCommentIdIn(commentIds: List<Long>)
}

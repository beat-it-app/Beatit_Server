package com.beat_it.post.repository

import com.beat_it.post.entity.PostComments
import com.beat_it.post.entity.enum.PostType
import org.springframework.data.jpa.repository.JpaRepository

interface PostCommentRepository : JpaRepository<PostComments, Long> {
    fun findByPostTypeAndPostIdOrderByCreatedAtAsc(postType: PostType, postId: Long): List<PostComments>
    fun deleteByPostTypeAndPostId(postType: PostType, postId: Long)
}

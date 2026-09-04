package com.beat_it.post.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.post.entity.enum.PostType
import jakarta.persistence.*

@Entity
@Table(name = "post_comments")
class PostComments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    val commentId: Long? = null,

    @Column(name = "post_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val postType: PostType,

    @Column(name = "post_id", nullable = false)
    val postId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 1000)
    var content: String,

    @Column(name = "parent_comment_id", nullable = true)
    val parentCommentId: Long? = null,

): BaseUpdatedTimeEntity(){
    companion object {
        fun createComment(
            postType: PostType,
            postId: Long,
            userId: Long,
            content: String,
            parentCommentId: Long? = null
        ): PostComments {
            return PostComments(
                postType = postType,
                postId = postId,
                userId = userId,
                content = content,
                parentCommentId = parentCommentId
            )
        }
    }
}

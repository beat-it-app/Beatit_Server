package com.beat_it.post.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "post_comment_mentions")
class PostCommentMentions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_mention_id")
    val commentMentionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    val comment: PostComments,

    @Column(name = "mentioned_user_id", nullable = false)
    val mentionedUserId: Long,

    @Column(name = "mentioned_name", nullable = false, length = 50)
    val mentionedName: String,

) : BaseCreatedTimeEntity() {
    companion object {
        fun create(comment: PostComments, mentionedUserId: Long, mentionedName: String): PostCommentMentions {
            return PostCommentMentions(
                comment = comment,
                mentionedUserId = mentionedUserId,
                mentionedName = mentionedName
            )
        }
    }
}

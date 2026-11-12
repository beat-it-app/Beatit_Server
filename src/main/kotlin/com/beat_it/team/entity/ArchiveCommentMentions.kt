package com.beat_it.team.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "archive_comment_mentions")
class ArchiveCommentMentions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_mention_id")
    val commentMentionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_comment_id", nullable = false)
    val comment: ArchiveComments,

    @Column(name = "mentioned_user_id", nullable = false)
    val mentionedUserId: Long,

    @Column(name = "mentioned_name", nullable = false, length = 50)
    val mentionedName: String,

) : BaseCreatedTimeEntity() {
    companion object {
        fun create(
            comment: ArchiveComments,
            mentionedUserId: Long,
            mentionedName: String,
        ): ArchiveCommentMentions {
            return ArchiveCommentMentions(
                comment = comment,
                mentionedUserId = mentionedUserId,
                mentionedName = mentionedName,
            )
        }
    }
}

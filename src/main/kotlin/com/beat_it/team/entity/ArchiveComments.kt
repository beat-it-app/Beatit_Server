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
@Table(name = "archives_comments")
class ArchiveComments(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_comment_id", nullable = false)
    val archiveCommentId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    val archive: Archives,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "content", nullable = false, length = 1000)
    var content: String,

) : BaseCreatedTimeEntity() {
    companion object {
        fun create(
            archive: Archives,
            userId: Long,
            content: String,
        ): ArchiveComments {
            return ArchiveComments(
                archive = archive,
                userId = userId,
                content = content,
            )
        }
    }
}

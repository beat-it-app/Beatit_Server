package com.beat_it.post.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*
import jakarta.persistence.GenerationType
import java.util.UUID

@Entity
@Table(name = "notices")
class Notices(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    val noticeId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Column(nullable = false, length = 50)
    var title: String,

    @Column(nullable = false, length = 5000)
    var content: String,

    @Column(name = "like_counter", nullable = false)
    var likeCounter: Int,

    @Column(name = "dislike_counter", nullable = false)
    var dislikeCounter: Int, // Counter라는 데이터타입도 있나봄

    @Column(name = "comment_counter", nullable = false)
    var commentCounter: Int,

    @Column(name = "thumbnail_image_url", length = 500)
    var thumbnailImageUrl: String? = null

    ) : BaseUpdatedTimeEntity() {
        companion object {
            fun writeNotice(userId: Long, teamId: Long, title: String, content: String, thumbnailImageUrl: String? = null): Notices {
                return Notices(
                    userId = userId,
                    teamId = teamId,
                    title = title,
                    content = content,
                    likeCounter = 0,
                    dislikeCounter = 0,
                    commentCounter = 0,
                    thumbnailImageUrl = thumbnailImageUrl
                )
            }
        }
}

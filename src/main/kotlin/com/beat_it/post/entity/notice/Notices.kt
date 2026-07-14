package com.beat_it.post.entity.notice

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*
import jakarta.persistence.GenerationType

@Entity
@Table(name = "notices")
class Notices(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    val noticeId: Long? = null,

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
    var dislikeCounter: Int,

    @Column(name = "comment_counter", nullable = false)
    var commentCounter: Int,

    @Column(name = "thumbnail_image_url")
    var thumbnailImageUrl: String? = null,

) : BaseUpdatedTimeEntity() {
    fun update(title: String, content: String, thumbnailImageUrl: String?) {
        this.title = title
        this.content = content
        this.thumbnailImageUrl = thumbnailImageUrl
    }

    fun increaseLike() {
        this.likeCounter++
    }

    fun decreaseLike() {
        if (this.likeCounter > 0) this.likeCounter--
    }

    fun increaseDislike() {
        this.dislikeCounter++
    }

    fun decreaseDislike() {
        if (this.dislikeCounter > 0) this.dislikeCounter--
    }

    fun increaseComment() {
        this.commentCounter++
    }

    fun decreaseComment() {
        if (this.commentCounter > 0) this.commentCounter--
    }

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


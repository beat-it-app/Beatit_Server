package com.beat_it.post.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.post.entity.enum.PollType
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "polls")
class Polls(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_id")
    val pollId: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "team_id", nullable = false)
    val teamId: Long,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "content", length = 500)
    var content: String?,

    @Column(name = "poll_type", nullable = false)
    var pollType: PollType,

    @Column(name = "allow_multiple_choice", nullable = false)
    var allowMultipleChoice: Boolean,

    @Column(name = "is_anonymous", nullable = false)
    var isAnonymous: Boolean,

    @Column(name = "remind_before_close", nullable = false)
    var remindBeforeClose: Boolean,

    @Column(name = "close_at")
    var closeAt: OffsetDateTime? = null,

    @Column(name = "poll_count")
    var pollCount: Int,

    @Column(name = "comment_counter", nullable = false)
    var commentCounter: Int

): BaseUpdatedTimeEntity() {
    @OneToMany(mappedBy = "poll", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    var pollOptions: List<PollOptions> = listOf()

    fun increaseComment() {
        this.commentCounter++
    }

    fun decreaseComment() {
        if (this.commentCounter > 0) this.commentCounter--
    }
    companion object {
        fun postPoll(userId: Long, teamId: Long, title: String, content: String?,
                     pollType: PollType, allowMultipleChoice: Boolean, isAnonymous: Boolean,
                     closeAt: OffsetDateTime?, remindBeforeClose: Boolean): Polls {
            return Polls(
                userId = userId,
                teamId = teamId,
                title = title,
                content = content,
                pollType = pollType,
                allowMultipleChoice = allowMultipleChoice,
                isAnonymous = isAnonymous,
                remindBeforeClose = remindBeforeClose,
                closeAt = closeAt,
                pollCount = 0,
                commentCounter = 0
            )
        }
    }
}
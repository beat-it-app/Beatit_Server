package com.beat_it.post.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "polls")
class Polls(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_id")
    val pollId: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "title", nullable = false, length = 200)
    var title: String,

    @Column(name = "content", length = 500)
    var content: String?,

    @Column(name = "allow_multiple_choice", nullable = false)
    var allowMultipleChoice: Boolean,

    @Column(name = "is_anonymous", nullable = false)
    var isAnonymous: Boolean,

    @Column(name = "remind_before_close", nullable = false)
    var remindBeforeClose: Boolean,

    @Column(name = "closes_at")
    var closesAt: LocalDateTime? = null

): BaseCreatedTimeEntity() {

}
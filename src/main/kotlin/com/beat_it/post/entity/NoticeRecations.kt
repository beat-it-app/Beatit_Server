package com.beat_it.post.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.post.entity.enum.ReactionType
import jakarta.persistence.*
import jakarta.persistence.GenerationType

class NoticeRecations(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_reaction_id")
    val noticeReactionId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "notice")
    val noticeId: Notices,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "reaction_type", nullable = false)
    var reactionType: ReactionType,

): BaseCreatedTimeEntity() {
}
package com.beat_it.post.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.post.entity.enum.ReactionType
import jakarta.persistence.*

@Entity
@Table(name = "notice_reactions")
class NoticeReactions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_reaction_id")
    val noticeReactionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    val notice: Notices,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "reaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var reactionType: ReactionType,

): BaseCreatedTimeEntity()

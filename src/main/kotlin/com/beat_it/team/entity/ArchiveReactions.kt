package com.beat_it.team.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.team.entity.enum.ReactionType
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
@Table(name = "archive_reactions")
class ArchiveReactions(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_reaction_id", nullable = false)
    val archiveReactionId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    val archive: Archives,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "reaction_type", nullable = false)
    val reactionType: ReactionType,

    //TODO: ReactedAt과 CreatedAt 둘 중 뭐를 선택할지!
    @Column(name = "reacted_at", nullable = false)
    var reactedAt: String,

): BaseCreatedTimeEntity() {}

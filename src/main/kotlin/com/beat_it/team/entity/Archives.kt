package com.beat_it.team.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "archives")
class Archives(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id", nullable = false)
    val archiveId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name = "title", nullable = true)
    var title: String,

    @Column(name = "place_name", nullable = true)
    var placeName: String? = null,

    @Column(name = "content", nullable = true)
    var content: String? = null,

    @Column(name = "like_count", nullable = false)
    var likeCount: String? = null,

    @Column(name = "dislike_count", nullable = false)
    var dislikeCount: String? = null,

    @Column(name = "comment_count", nullable = false)
    var commentCount: String? = null,
): BaseUpdatedTimeEntity() {}

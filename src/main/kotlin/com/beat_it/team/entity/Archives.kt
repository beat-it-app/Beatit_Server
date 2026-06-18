package com.beat_it.team.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.team.entity.enum.TeamType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "archives")
class Archives(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id", nullable = false)
    val archiveId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name = "user_id", nullable = false)
    val authorId: Long,

    //TODO: Location id 추가할 것.
    @Column(name = "location_id", nullable = false)
    var locationId: Long,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "place_name", nullable = true)
    var placeName: String? = null,

    @Column(name = "description", nullable = true)
    var description: String? = null,

    @Column(name = "archive_image_url", nullable = true)
    var archiveImageUrl: String? = null,

    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,

    @Column(name = "dislike_count", nullable = false)
    var dislikeCount: Int = 0,

    @Column(name = "comment_count", nullable = false)
    var commentCount: Int = 0,
): BaseUpdatedTimeEntity() {

    fun updateArchive(
        title: String?,
        description: String?,
        placeName: String?,
        locationId: Long?,
        archiveImageUrl: String?,
    ) {
        title?.let { this.title = it }
        description?.let { this.description = it }
        placeName?.let { this.placeName = it }
        locationId?.let { this.locationId = it }
        archiveImageUrl?.let { this.archiveImageUrl = it }
    }
}
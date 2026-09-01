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
    val writerId: Long,

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

    @Column(name = "rating_sum", nullable = false)
    var ratingSum: Int = 0,

    @Column(name = "rating_count", nullable = false)
    var ratingCount: Int = 0,

    @Column(name = "comment_count", nullable = false)
    var commentCount: Int = 0,
): BaseUpdatedTimeEntity() {

    fun updateArchive(
        title: String?,
        description: String?,
        placeName: String?,
        locationId: Long?,
    ) {
        title?.let { this.title = it }
        description?.let { this.description = it }
        placeName?.let { this.placeName = it }
        locationId?.let { this.locationId = it }
    }

    fun updateArchiveImageUrl(archiveImageUrl: String?) {
        this.archiveImageUrl = archiveImageUrl
    }

    fun addRating(score: Int) {
        ratingSum += score
        ratingCount++
    }

    fun updateRating(previousScore: Int, newScore: Int) {
        ratingSum += newScore - previousScore
    }

    fun calculateAverageRating(): Double {
        if (ratingCount == 0) {
            return 0.0
        }

        return Math.round((ratingSum.toDouble() / ratingCount) * 10) / 10.0
    }

    fun increaseComment() {
        commentCount++
    }

    fun decreaseComment() {
        if (commentCount > 0) {
            commentCount--
        }
    }
}

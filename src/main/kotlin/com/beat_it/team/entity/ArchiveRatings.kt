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
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "archive_ratings",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_archive_ratings_archive_user",
            columnNames = ["archive_id", "user_id"],
        ),
    ],
)
class ArchiveRatings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_rating_id", nullable = false)
    val archiveRatingId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    val archive: Archives,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "score", nullable = false)
    var score: Int,
) : BaseUpdatedTimeEntity() {

    fun updateScore(score: Int) {
        this.score = score
    }
}

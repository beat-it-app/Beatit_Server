package com.beat_it.team.repository

import com.beat_it.team.entity.ArchiveRatings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArchiveRatingsRepository : JpaRepository<ArchiveRatings, Long> {
    fun findByArchiveArchiveIdAndUserId(
        archiveId: Long,
        userId: Long,
    ): ArchiveRatings?

    @Query(
        """
        SELECT AVG(r.score)
        FROM ArchiveRatings r
        WHERE r.archive.archiveId = :archiveId
        """
    )
    fun findAverageScoreByArchiveId(
        @Param("archiveId") archiveId: Long,
    ): Double?

    fun deleteByArchiveArchiveId(archiveId: Long): Int
}

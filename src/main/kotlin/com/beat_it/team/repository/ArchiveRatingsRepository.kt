package com.beat_it.team.repository

import com.beat_it.team.entity.ArchiveRatings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchiveRatingsRepository : JpaRepository<ArchiveRatings, Long> {
    fun findByArchiveArchiveIdAndUserId(
        archiveId: Long,
        userId: Long,
    ): ArchiveRatings?

    fun deleteByArchiveArchiveId(archiveId: Long): Int
}

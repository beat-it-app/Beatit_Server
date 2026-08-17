package com.beat_it.team.repository

import com.beat_it.team.entity.ArchiveReactions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchiveReactionsRepository : JpaRepository<ArchiveReactions, Long> {
    fun findByArchiveArchiveIdAndUserId(
        archiveId: Long,
        userId: Long,
    ): ArchiveReactions?

    fun deleteByArchiveArchiveId(archiveId: Long): Int
}

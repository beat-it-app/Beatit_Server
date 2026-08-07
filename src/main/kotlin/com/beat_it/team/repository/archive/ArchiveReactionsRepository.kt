package com.beat_it.team.repository.archive

import com.beat_it.team.entity.archive.ArchiveReactions
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchiveReactionsRepository : JpaRepository<ArchiveReactions, Long> {
//    fun findByArchiveId(archiveId: Long): Archives?

    fun deleteByArchiveArchiveId(archiveId: Long): Int
}
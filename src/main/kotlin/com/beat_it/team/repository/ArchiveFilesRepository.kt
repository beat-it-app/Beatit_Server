package com.beat_it.team.repository

import com.beat_it.team.entity.ArchivesFiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchivesFilesRepository : JpaRepository<ArchivesFiles, Long> {
    fun findAllByArchiveArchiveIdOrderByArchiveFileIdAsc(archiveId: Long): List<ArchivesFiles>

    fun deleteAllByArchiveArchiveId(archiveId: Long)
}

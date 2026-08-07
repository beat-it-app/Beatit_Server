package com.beat_it.team.repository.archive

import com.beat_it.team.entity.archive.ArchivesFiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchivesFilesRepository : JpaRepository<ArchivesFiles, Long> {
    fun deleteAllByArchiveArchiveId(archiveId: Long)
}
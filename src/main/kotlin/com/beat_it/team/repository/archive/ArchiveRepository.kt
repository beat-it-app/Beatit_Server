package com.beat_it.team.repository.archive

import com.beat_it.team.entity.archive.Archives
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchiveRepository : JpaRepository<Archives, Long> {
    fun findByArchiveId(archiveId: Long): Archives?

}
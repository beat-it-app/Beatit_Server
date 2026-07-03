package com.beat_it.team.repository

import com.beat_it.team.entity.Archives
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArchiveRepository : JpaRepository<Archives, Long> {
    fun findByArchiveId(archiveId: Long): Archives?

}
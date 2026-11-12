package com.beat_it.team.repository

import com.beat_it.team.entity.ArchiveComments
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArchiveCommentsRepository : JpaRepository<ArchiveComments, Long> {
    fun findAllByArchiveArchiveIdOrderByCreatedAtAsc(archiveId: Long): List<ArchiveComments>

    fun findByArchiveCommentIdAndArchiveArchiveId(
        archiveCommentId: Long,
        archiveId: Long,
    ): ArchiveComments?

    fun findByParentCommentId(parentCommentId: Long): List<ArchiveComments>

    @Query(
        """
        SELECT c.archive.archiveId AS archiveId, COUNT(c) AS commentCount
        FROM ArchiveComments c
        WHERE c.archive.archiveId IN :archiveIds
        GROUP BY c.archive.archiveId
        """
    )
    fun countCommentsByArchiveIds(
        @Param("archiveIds") archiveIds: Collection<Long>,
    ): List<ArchiveCommentCount>

    fun deleteByArchiveArchiveId(archiveId: Long): Int
}

interface ArchiveCommentCount {
    val archiveId: Long
    val commentCount: Long
}

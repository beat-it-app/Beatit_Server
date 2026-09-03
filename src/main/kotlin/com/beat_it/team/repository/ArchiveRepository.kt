package com.beat_it.team.repository

import com.beat_it.team.entity.Archives
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArchiveRepository : JpaRepository<Archives, Long> {
    fun findByArchiveId(archiveId: Long): Archives?

    fun findAllByTeamTeamId(
        teamId: Long,
        pageable: Pageable,
    ): Page<Archives>

    @Query(
        """
        SELECT a
        FROM Archives a
        WHERE a.team.teamId = :teamId
          AND a.ratingCount > 0
        ORDER BY (a.ratingSum * 1.0 / a.ratingCount) DESC,
                 a.ratingCount DESC,
                 a.createdAt DESC,
                 a.archiveId DESC
        """
    )
    fun findTopRatedByTeamId(
        @Param("teamId") teamId: Long,
        pageable: Pageable,
    ): List<Archives>
}

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
        value = """
            SELECT a
            FROM Archives a
            WHERE a.team.teamId = :teamId
            ORDER BY CASE WHEN a.ratingCount = 0 THEN 1 ELSE 0 END ASC,
                     CASE WHEN a.ratingCount = 0 THEN 0.0 ELSE (a.ratingSum * 1.0 / a.ratingCount) END DESC,
                     a.ratingCount DESC,
                     a.createdAt DESC,
                     a.archiveId DESC
        """,
        countQuery = """
            SELECT COUNT(a)
            FROM Archives a
            WHERE a.team.teamId = :teamId
        """,
    )
    fun findAllByTeamTeamIdOrderByRatingDesc(
        @Param("teamId") teamId: Long,
        pageable: Pageable,
    ): Page<Archives>

    @Query(
        value = """
            SELECT a
            FROM Archives a
            WHERE a.team.teamId = :teamId
            ORDER BY CASE WHEN a.ratingCount = 0 THEN 1 ELSE 0 END ASC,
                     CASE WHEN a.ratingCount = 0 THEN 0.0 ELSE (a.ratingSum * 1.0 / a.ratingCount) END ASC,
                     a.ratingCount DESC,
                     a.createdAt DESC,
                     a.archiveId DESC
        """,
        countQuery = """
            SELECT COUNT(a)
            FROM Archives a
            WHERE a.team.teamId = :teamId
        """,
    )
    fun findAllByTeamTeamIdOrderByRatingAsc(
        @Param("teamId") teamId: Long,
        pageable: Pageable,
    ): Page<Archives>

    @Query(
        """
        SELECT a.archiveId
        FROM Archives a
        WHERE a.team.teamId = :teamId
          AND a.ratingCount > 0
        ORDER BY (a.ratingSum * 1.0 / a.ratingCount) DESC,
                 a.ratingCount DESC,
                 a.createdAt DESC,
                 a.archiveId DESC
        """
    )
    fun findTopRatedArchiveIdsByTeamId(
        @Param("teamId") teamId: Long,
        pageable: Pageable,
    ): List<Long>
}

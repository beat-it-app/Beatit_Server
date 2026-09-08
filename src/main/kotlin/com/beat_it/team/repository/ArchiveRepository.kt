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

    fun findAllByTeamTeamId(teamId: Long): List<Archives>

    @Query(
        value = """
            SELECT a
            FROM Archives a
            WHERE a.team.teamId = :teamId
            ORDER BY CASE WHEN a.ratingCount = 0 THEN 1 ELSE 0 END ASC,
                     a.averageRating DESC,
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
                     a.averageRating ASC,
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
}

package com.beat_it.team.repository

import com.beat_it.team.dto.CategoryStorageUsageProjection
import com.beat_it.team.entity.TeamCloudFolder
import com.beat_it.team.entity.TeamCloudItem
import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TeamCloudItemRepository : JpaRepository<TeamCloudItem, Long> {
    @Query("SELECT i FROM TeamCloudItem i WHERE i.team = :team AND i.teamCloudFolder IS NULL")
    fun findByTeamAndTeamCloudFolderIsNull(team: Teams): List<TeamCloudItem>
    fun findByTeamCloudFolder(teamCloudFolder: TeamCloudFolder): List<TeamCloudItem>
    fun findByTeam(team: Teams): List<TeamCloudItem>

    @Query("""
        SELECT tf.mediaCategory AS category, COALESCE(SUM(tf.fileSizeBytes), 0L) AS totalBytes
        FROM TeamCloudItem tci
        JOIN tci.teamFile tf
        WHERE tci.team.teamId = :teamId
        GROUP BY tf.mediaCategory
    """)
    fun findStorageUsageByCategory(@Param("teamId") teamId: Long): List<CategoryStorageUsageProjection>

    @Query("""
        SELECT COUNT(tci) FROM TeamCloudItem tci
        WHERE tci.uploaderId = :userId
          AND tci.teamFile IS NOT NULL
    """)
    fun countUploadedFilesByUserId(@Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(tci) FROM TeamCloudItem tci
        WHERE tci.team.teamId = :teamId
          AND tci.uploaderId = :userId
          AND tci.teamFile IS NOT NULL
    """)
    fun countUploadedFilesByTeamIdAndUserId(@Param("teamId") teamId: Long, @Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(tci) FROM TeamCloudItem tci
        WHERE tci.team.teamId = :teamId
          AND tci.teamFile IS NOT NULL
    """)
    fun countUploadedFilesByTeamId(@Param("teamId") teamId: Long): Long

    @Query("""
        SELECT COUNT(tci) FROM TeamCloudItem tci
        JOIN tci.teamFile tf
        WHERE tci.uploaderId = :userId
          AND tf.mediaCategory IN (com.beat_it.team.entity.enum.MediaCategory.DOCUMENT, com.beat_it.team.entity.enum.MediaCategory.AUDIO)
    """)
    fun countScoresAndMusicsByUserId(@Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(tci) FROM TeamCloudItem tci
        JOIN tci.teamFile tf
        WHERE tci.team.teamId = :teamId
          AND tci.uploaderId = :userId
          AND tf.mediaCategory IN (com.beat_it.team.entity.enum.MediaCategory.DOCUMENT, com.beat_it.team.entity.enum.MediaCategory.AUDIO)
    """)
    fun countScoresAndMusicsByTeamIdAndUserId(@Param("teamId") teamId: Long, @Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(tci) FROM TeamCloudItem tci
        JOIN tci.teamFile tf
        WHERE tci.team.teamId = :teamId
          AND tf.mediaCategory IN (com.beat_it.team.entity.enum.MediaCategory.DOCUMENT, com.beat_it.team.entity.enum.MediaCategory.AUDIO)
    """)
    fun countScoresAndMusicsByTeamId(@Param("teamId") teamId: Long): Long
}
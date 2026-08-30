package com.beat_it.team.repository

import com.beat_it.team.entity.TeamCloudFolder
import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TeamCloudFolderRepository : JpaRepository<TeamCloudFolder, Long> {
    @Query("SELECT f FROM TeamCloudFolder f WHERE f.team = :team")
    fun findByTeam(team: Teams): List<TeamCloudFolder>

    fun existsByTeamAndFolderName(team: Teams, folderName: String): Boolean
}
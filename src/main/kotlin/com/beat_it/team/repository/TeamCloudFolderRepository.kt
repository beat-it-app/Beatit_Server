package com.beat_it.team.repository

import com.beat_it.team.entity.TeamCloudFolders
import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TeamCloudFolderRepository : JpaRepository<TeamCloudFolders, Long> {
    @Query("SELECT f FROM TeamCloudFolders f WHERE f.team = :team")
    fun findByTeam(team: Teams): List<TeamCloudFolders>

    fun existsByTeamAndFolderName(team: Teams, folderName: String): Boolean
}
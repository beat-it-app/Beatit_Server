package com.beat_it.team.repository

import com.beat_it.team.entity.TeamCloudFolder
import com.beat_it.team.entity.TeamCloudItem
import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TeamCloudItemRepository : JpaRepository<TeamCloudItem, Long> {
    @Query("SELECT i FROM TeamCloudItem i WHERE i.team = :team AND i.teamCloudFolder IS NULL")
    fun findByTeamAndTeamCloudFolderIsNull(team: Teams): List<TeamCloudItem>
    fun findByTeamCloudFolder(teamCloudFolder: TeamCloudFolder): List<TeamCloudItem>
    fun findByTeam(team: Teams): List<TeamCloudItem>
}
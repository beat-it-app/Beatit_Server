package com.beat_it.team.repository

import com.beat_it.team.entity.TeamCloudFolders
import com.beat_it.team.entity.TeamCloudItems
import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TeamCloudItemsRepository : JpaRepository<TeamCloudItems, Long> {
    @Query("SELECT i FROM TeamCloudItems i WHERE i.team = :team AND i.teamCloudFolder IS NULL")
    fun findByTeamAndTeamCloudFolderIsNull(team: Teams): List<TeamCloudItems>
    fun findByTeamCloudFolder(teamCloudFolder: TeamCloudFolders): List<TeamCloudItems>
    fun findByTeam(team: Teams): List<TeamCloudItems>
}
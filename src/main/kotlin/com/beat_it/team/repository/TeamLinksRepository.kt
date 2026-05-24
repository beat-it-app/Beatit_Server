package com.beat_it.team.repository

import com.beat_it.team.entity.TeamLinks
import org.springframework.data.jpa.repository.JpaRepository

interface TeamLinksRepository : JpaRepository<TeamLinks, Long> {

    fun findAllByTeamId(
        teamId: Long,
    ): List<TeamLinks>

    fun deleteAllByTeamId(teamId: Long)

}
package com.beat_it.team.repository

import com.beat_it.team.entity.TeamParts
import org.springframework.data.jpa.repository.JpaRepository

interface TeamPartsRepository : JpaRepository<TeamParts, Long> {

    fun findAllByTeamId(
        teamId: Long,
    ): List<TeamParts>

    fun deleteAllByTeamId(teamId: Long)

}
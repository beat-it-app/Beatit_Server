package com.beat_it.team.repository

import com.beat_it.team.entity.TeamParts
import org.springframework.data.jpa.repository.JpaRepository

interface TeamPartsRepository : JpaRepository<TeamParts, Long> {

    fun findAllByTeamTeamId(
        teamId: Long,
    ): List<TeamParts>

    fun deleteAllByTeamTeamId(
        teamId: Long,
    )
}
package com.beat_it.team.repository

import com.beat_it.team.entity.TeamParts
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamPartsRepository : JpaRepository<TeamParts, Long> {

    fun findAllByTeamTeamId(
        teamId: Long,
    ): List<TeamParts>

    fun findAllByTeamTeamIdAndIsActiveTrueOrderByDisplayOrderAscTeamPartIdAsc(
        teamId: Long,
    ): List<TeamParts>

    fun deleteAllByTeamTeamId(
        teamId: Long,
    )
}

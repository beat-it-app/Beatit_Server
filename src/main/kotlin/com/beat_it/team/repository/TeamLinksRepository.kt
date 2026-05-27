package com.beat_it.team.repository

import com.beat_it.team.entity.TeamLinks
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamLinksRepository : JpaRepository<TeamLinks, Long> {

    fun findAllByTeamTeamId(
        teamId: Long,
    ): List<TeamLinks>

    fun deleteAllByTeamTeamId(
        teamId: Long,
    )
}
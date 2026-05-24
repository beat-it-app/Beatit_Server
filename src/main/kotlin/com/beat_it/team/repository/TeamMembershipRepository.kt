package com.beat_it.team.repository

import com.beat_it.team.entity.TeamMemberships
import org.springframework.data.jpa.repository.JpaRepository

interface TeamMembershipRepository : JpaRepository<TeamMemberships, Long> {

    fun findByTeamIdAndUserIdAndLeaftAtIsNull(
        teamId: Long,
        userId: Long,
    ): TeamMemberships?

    fun countByTeamIdAndLeftAtIsNull(teamId: Long): Int

}
package com.beat_it.team.repository

import com.beat_it.team.entity.TeamMemberships
import org.springframework.data.jpa.repository.JpaRepository

interface TeamMembershipRepository : JpaRepository<TeamMemberships, Long> {

    fun findByTeamTeamIdAndUserIdAndLeftAtIsNull(
        teamId: Long,
        userId: Long,
    ): TeamMemberships?

    fun countByTeamTeamIdAndLeftAtIsNull(
        teamId: Long,
    ): Int
}
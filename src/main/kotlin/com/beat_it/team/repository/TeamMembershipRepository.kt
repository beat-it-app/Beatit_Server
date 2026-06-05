package com.beat_it.team.repository

import com.beat_it.team.entity.TeamMemberships
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMembershipRepository : JpaRepository<TeamMemberships, Long> {

    fun findByTeamTeamIdAndUserIdAndLeftAtIsNull(
        teamId: Long,
        userId: Long,
    ): TeamMemberships?

    fun countByTeamTeamIdAndLeftAtIsNull(
        teamId: Long,
    ): Int

    fun findAllByUserIdAndLeftAtIsNull(
        userId: Long,
    ): List<TeamMemberships>
}
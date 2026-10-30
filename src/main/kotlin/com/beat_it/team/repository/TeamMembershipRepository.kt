package com.beat_it.team.repository

import com.beat_it.team.entity.TeamMemberships
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMembershipRepository : JpaRepository<TeamMemberships, Long> {

    fun findByTeamTeamIdAndUserId(
        teamId: Long,
        userId: Long,
    ): TeamMemberships?

    fun existsByTeamTeamIdAndUserId(
        teamId: Long,
        userId: Long
    ): Boolean

    fun findAllByTeamTeamId(teamId: Long): List<TeamMemberships>

    fun findTop10ByTeamTeamIdOrderByJoinedAtAsc(teamId: Long): List<TeamMemberships>

    fun findAllByUserIdAndTeamDeletedAtIsNullOrderByJoinedAtDesc(
        userId: Long
    ): List<TeamMemberships>

    fun countByTeamTeamId(
        teamId: Long,
    ): Int

    fun findAllByTeamTeamIdAndUserIdIn(
        teamId: Long,
        userIds: List<Long>
    ): List<TeamMemberships>

    fun findAllByUserId(userId: Long): List<TeamMemberships>

    fun countByUserId(userId: Long): Long
}

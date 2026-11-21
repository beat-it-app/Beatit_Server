package com.beat_it.team.repository

import com.beat_it.team.entity.TeamMemberships
import com.beat_it.team.entity.enum.TeamRole
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    @Query("""
        SELECT tm FROM TeamMemberships tm
        JOIN Users u ON tm.userId = u.userId
        WHERE tm.team.teamId = :teamId
          AND u.accountStatus = com.beat_it.auth.entity.enum.AccountStatus.ACTIVE
    """)
    fun findAllActiveByTeamId(@Param("teamId") teamId: Long): List<TeamMemberships>

    @Query("""
        SELECT tm FROM TeamMemberships tm
        JOIN Users u ON tm.userId = u.userId
        WHERE tm.team.teamId = :teamId
          AND u.accountStatus = com.beat_it.auth.entity.enum.AccountStatus.ACTIVE
        ORDER BY tm.joinedAt ASC
    """)
    fun findTop10ActiveByTeamId(
        @Param("teamId") teamId: Long,
        pageable: Pageable
    ): List<TeamMemberships>

    fun findAllByUserIdAndTeamDeletedAtIsNullOrderByJoinedAtDesc(
        userId: Long
    ): List<TeamMemberships>

    @Query("""
        SELECT COUNT(tm) FROM TeamMemberships tm
        JOIN Users u ON tm.userId = u.userId
        WHERE tm.team.teamId = :teamId
          AND u.accountStatus = com.beat_it.auth.entity.enum.AccountStatus.ACTIVE
    """)
    fun countByTeamTeamId(
        @Param("teamId") teamId: Long,
    ): Int

    fun findAllByTeamTeamIdAndUserIdIn(
        teamId: Long,
        userIds: List<Long>
    ): List<TeamMemberships>

    @Query("""
        SELECT tm FROM TeamMemberships tm
        JOIN Users u ON tm.userId = u.userId
        WHERE tm.team.teamId = :teamId
          AND tm.userId IN :userIds
          AND u.accountStatus = com.beat_it.auth.entity.enum.AccountStatus.ACTIVE
    """)
    fun findAllActiveByTeamIdAndUserIdIn(
        @Param("teamId") teamId: Long,
        @Param("userIds") userIds: Collection<Long>
    ): List<TeamMemberships>

    fun findAllByUserId(userId: Long): List<TeamMemberships>

    fun countByUserId(userId: Long): Long

    @Query("SELECT COUNT(tm) > 0 FROM TeamMemberships tm WHERE tm.userId = :userId AND tm.teamRole = :teamRole AND tm.team.deletedAt IS NULL")
    fun existsByUserIdAndTeamRoleAndTeamDeletedAtIsNull(@Param("userId") userId: Long, @Param("teamRole") teamRole: TeamRole): Boolean
}

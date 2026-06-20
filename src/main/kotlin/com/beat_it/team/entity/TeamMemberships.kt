package com.beat_it.team.entity

import com.beat_it.global.entity.BaseJoinedTimeEntity
import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.team.entity.enum.TeamRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_membership")
class TeamMemberships(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_membership_id")
    val teamMembershipId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "team_role", nullable = false)
    var teamRole: TeamRole = TeamRole.MEMBER,

    @Column(name = "left_at", nullable = true)
    var leftAt: OffsetDateTime? = null,
) : BaseJoinedTimeEntity() {

    fun updateTeamRole(teamRole: TeamRole) {
        this.teamRole = teamRole
    }

    fun leaveTeam() {
        this.leftAt = OffsetDateTime.now()
    }
}
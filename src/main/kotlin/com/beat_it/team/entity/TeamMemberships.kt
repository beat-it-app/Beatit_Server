package com.beat_it.team.entity

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

    //TODO: 팀 ID 연결하기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    //TODO: 사용자 ID 연결하기 >> 모듈 분리

    @Column(name="team_role", nullable = false)
    var teamRole: TeamRole = TeamRole.MEMBER,

    @Column(name="left_at", nullable = true)
    var leftAt: OffsetDateTime? = null,

) : BaseUpdatedTimeEntity() {
    // TODO: 팀 권한 설정 함수 만들기
    fun updateTeamRole(teamRole: TeamRole) {
        this.teamRole = teamRole
    }

    fun leaveTeam() {
        this.leftAt = OffsetDateTime.now()
    }
}
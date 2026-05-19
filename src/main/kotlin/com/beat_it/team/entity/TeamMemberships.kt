package com.beat_it.team.entity

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
    //TODO: 사용자 ID 연결하기 >> 모듈 분리

    @Column(name="team_role", nullable = false)
    var teamRole: TeamRole = TeamRole.MEMBER,

    @Column(name="update_at", nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name="create_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name="left_at", nullable = true)
    var leftAt: OffsetDateTime? = null,
)
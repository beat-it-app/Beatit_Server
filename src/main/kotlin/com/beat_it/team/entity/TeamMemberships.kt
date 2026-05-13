package com.beat_it.team.entity

import com.beat_it.team.entity.enum.TeamRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_membership")
class TeamMemberships(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val teamMembershipId: Long? = null,

    //TODO: 팀 ID 연결하기
    //TODO: 사용자 ID 연결하기 >> 모듈 분리

    @Column(nullable = false)
    var teamRole: TeamRole = TeamRole.MEMBER,

    @Column(nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = true)
    var leftAt: OffsetDateTime? = null,
)
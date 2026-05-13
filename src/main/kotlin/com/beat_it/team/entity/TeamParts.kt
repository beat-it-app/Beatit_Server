package com.beat_it.team.entity

import com.beat_it.team.entity.enum.TeamRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_parts")
class TeamParts(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val teamPartId: Long? = null,

    //TODO: 팀 ID 연결하기

    @Column(nullable = false)
    var partName: String = "",

    @Column(nullable = false)
    var displayOrder: Int = 0,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    )
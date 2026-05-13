package com.beat_it.team.entity

import com.beat_it.team.entity.enum.PlatformCode
import com.beat_it.team.entity.enum.TeamRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_links")
class TeamLinks(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    val teamLinkId: Long? = null,

    //TODO: 팀 ID 연결하기

    @Column(nullable = false)
    var partName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var platformCode: PlatformCode = PlatformCode.CUSTOM,

    @Column(nullable = false)
    var linkUrl: String = "",

    @Column(nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    )
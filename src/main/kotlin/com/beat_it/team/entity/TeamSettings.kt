package com.beat_it.team.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_settings")
class TeamSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val teamSettingId: Long? = null,

    //TODO: 팀 ID 연결하기

    @Column(nullable = false)
    var maxStorage: Int = 10,

    @Column(nullable = false)
    var usedStorage: Int = 0,

    @Column(nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),
)
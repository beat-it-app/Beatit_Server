package com.beat_it.team.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_settings")
class TeamSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_settings_id")
    val teamSettingId: Long? = null,

    //TODO: 팀 ID 연결하기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name="max_storage", nullable = false)
    var maxStorage: Int = 10,

    @Column(name="used_storage", nullable = false)
    var usedStorage: Int = 0,

    @Column(name="update_at", nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),
)
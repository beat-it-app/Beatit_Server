package com.beat_it.team.entity

import com.beat_it.team.entity.enum.TeamRole
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "team_parts")
class TeamParts(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_part_id", nullable = false)
    val teamPartId: Long? = null,

    //TODO: 팀 ID 연결하기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name="part_name", nullable = false)
    var partName: String = "",

    @Column(name="display_order", nullable = false)
    var displayOrder: Int = 0,

    @Column(name="is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name="update_at", nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name="create_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    )
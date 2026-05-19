package com.beat_it.team.entity

import com.beat_it.team.entity.enum.TeamType
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "teams")
class Teams(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="team_id", nullable = false)
    val teamId: Long? = null,

    @Column(name="public_at", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(), // 외부 노출용 ID 

    @Column(name="name", nullable = false)
    var teamName: String = "",

    @Column(name="description", nullable = true)
    var description: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name="team_type", nullable = false)
    val teamType: TeamType = TeamType.TEAM,

    @Column(name="established_on", nullable = false)
    var establishedOn: OffsetDateTime? = null,

    @Column(name="invite_code", nullable = false, unique = true)
    val inviteCode: String = "",

    @Column(name="update_at", nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name="create_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name="deleted_at", nullable = true)
    var deletedAt: OffsetDateTime? = null,

)

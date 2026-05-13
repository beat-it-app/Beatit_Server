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
    val teamId: Long? = null,

    @Column(nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(), // 외부 노출용 ID 

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = true)
    var description: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val teamType: TeamType = TeamType.TEAM,

    @Column(nullable = false)
    var establishedOn: OffsetDateTime? = null,

    @Column(nullable = false, unique = true)
    val inviteCode: String = "",

    @Column(nullable = false)
    var updateAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = true)
    var deleteddAt: OffsetDateTime? = null,

)

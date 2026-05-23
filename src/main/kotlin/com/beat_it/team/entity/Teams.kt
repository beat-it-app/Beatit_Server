package com.beat_it.team.entity

import com.beat_it.global.entity.BaseTimeEntity
import com.beat_it.team.entity.enum.TeamType
import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "teams")
class Teams(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id", nullable = false)
    val teamId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "profile_image_url", nullable = true)
    var profileImageUrl: String? = null,

    @Column(name = "name", nullable = false)
    var teamName: String,

    @Column(name = "description", nullable = true)
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "team_type", nullable = false)
    var teamType: TeamType = TeamType.TEAM,

    @Column(name = "established_on", nullable = true)
    var establishedOn: LocalDate? = null,

    @Column(name = "invite_code", nullable = false, unique = true)
    val inviteCode: String,

    @Column(name = "deleted_at", nullable = true)
    var deletedAt: OffsetDateTime? = null,

) : BaseTimeEntity() {

    fun updateTeamDetail (
        teamName: String,
        description: String?,
        establishedOn: LocalDate?,
        teamType: TeamType,
    ) {
        this.teamName = teamName
        this.description = description
        this.establishedOn = establishedOn
        this.teamType = teamType
    }


    fun deleteTeam() {
        this.deletedAt = OffsetDateTime.now()
    }
}
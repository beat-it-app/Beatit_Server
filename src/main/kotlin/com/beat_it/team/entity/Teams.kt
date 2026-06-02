package com.beat_it.team.entity
import com.beat_it.global.entity.BaseDeletedTimeEntity
import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.team.entity.enum.TeamType
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "teams")
// 1. repository.delete() 호출 시 실행될 쿼리 지정 (자동화의 핵심)
@SQLDelete(sql = "UPDATE teams SET deleted_at = CURRENT_TIMESTAMP WHERE team_id = ?")
// 2. 조회 시 삭제된 데이터는 자동으로 필터링 (과거 @Where 역할)
@SQLRestriction("deleted_at IS NULL")
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


) : BaseDeletedTimeEntity() {

    fun updateTeamDetail(
        teamName: String?,
        description: String?,
        establishedOn: LocalDate?,
        teamType: TeamType?,
    ) {
        teamName?.let { this.teamName = it }
        description?.let { this.description = it }
        establishedOn?.let { this.establishedOn = it }
        teamType?.let { this.teamType = it }
    }
}
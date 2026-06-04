package com.beat_it.auth.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.auth.entity.enum.AccountStatus
import com.beat_it.auth.entity.enum.Role
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class Users (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role,

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    var accountStatus: AccountStatus,

    @Column(name = "withdrawn_at")
    var withdrawnAt: OffsetDateTime? = null,

    @Column(name = "current_team_id")
    var currentTeamId: Long? = null
    
) : BaseCreatedTimeEntity() {
    companion object {
        fun createNewUser(
            role: Role,
            accountStatus: AccountStatus
        ): Users {
            return Users(
                role = role,
                accountStatus = accountStatus
            )
        }
    }

    fun updateCurrentTeam(teamId: Long) {
        this.currentTeamId = teamId
    }
}
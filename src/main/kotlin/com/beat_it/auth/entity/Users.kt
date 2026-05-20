package com.beat_it.auth.entity

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
    val publicId: UUID = UUID.randomUUID(), // 외부 노출용 ID

    @Column(nullable = false)
    var role: Role,

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    var accountStatus: AccountStatus,

    @Column(name = "withdrawn_at")
    var withdrawnAt: OffsetDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime
) {
    companion object {
        fun createNewUser(
            role: Role,
            accountStatus: AccountStatus,
            createdAt: OffsetDateTime
        ): Users {
            return Users(
                role = role,
                accountStatus = accountStatus,
                createdAt = createdAt
            )
        }
    }
}
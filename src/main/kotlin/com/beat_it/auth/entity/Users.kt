package com.beat_it.auth.entity 

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "users")
class Users(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(), // 외부 노출용 ID 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var accountStatus: AccountStatus = AccountStatus.ACTIVE,

    @Column(nullable = true)
    var withdrawnAt: OffsetDateTime? = null,

    @Column(nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

package com.beat_it.auth.entity

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.time.ZoneId

@Entity
@Table(name = "user_setting")
class UsersSetting (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userSettingId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val users: Users,

    @Column
    var lastTeamId: Long? = null,

    @Column(nullable = false)
    var allowAutoLogin: Boolean = false,

    @Column(nullable = false)
    var timezone: String = ZoneId.systemDefault().id,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
package com.beat_it.auth.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_settings")
class UserSettings (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userSettingId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val users: Users,

    @Column
    var lastTeamId: Long? = null,

    @Column(nullable = false)
    var allowAutoLogin: Boolean

) : BaseUpdatedTimeEntity() {
    companion object {
        fun createNewUser(
            user: Users,
            allowAutoLogin: Boolean
        ): UserSettings {
            return UserSettings(
                users = user,
                allowAutoLogin = allowAutoLogin
            )
        }
    }
}
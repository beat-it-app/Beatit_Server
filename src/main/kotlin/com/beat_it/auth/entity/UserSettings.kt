package com.beat_it.auth.entity

import com.beat_it.auth.entity.enum.SocialProvider
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

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
    var allowAutoLogin: Boolean,

    @UpdateTimestamp
    @Column(nullable = false)
    var updatedAt: OffsetDateTime,
) {
    companion object {
        fun createNewUser(
            user: Users,
        ): UserSettings {
            return UserSettings(
                users = user,
                allowAutoLogin = false,
                updatedAt = OffsetDateTime.now()
            )
        }
    }
}
package com.beat_it.auth.entity 

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_profiles")
class UserProfiles(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id", nullable = false)
    val userProfileId: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: Users? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image", nullable = false)
    var authFile: AuthFiles,

    @Column(nullable = false, length = 10)
    var name: String

) : BaseUpdatedTimeEntity() {
    companion object {
        fun create(
            user: Users,
            name: String,
            authFile: AuthFiles
        ): UserProfiles {
            return UserProfiles(
                user = user,
                name = name,
                authFile = authFile
            )
        }
    }
}
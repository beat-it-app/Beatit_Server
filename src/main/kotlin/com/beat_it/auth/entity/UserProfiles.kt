package com.beat_it.auth.entity 

import jakarta.persistence.*
import java.time.OffsetDateTime

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

    @OneToOne
    @JoinColumn(name = "profile_image", nullable = false)
    var authFile: AuthFiles? = null,

    @Column(nullable = false)
    var name: String,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)
package com.beat_it.notification.entity

import com.beat_it.global.entity.BaseUpdatedTimeEntity
import com.beat_it.notification.entity.enum.PlatformType
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "push_tokens",
    indexes = [
        Index(name = "idx_push_tokens_user_id", columnList = "user_id"),
        Index(name = "idx_push_tokens_device_id", columnList = "device_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_push_tokens_device_user", columnNames = ["device_id", "user_id"])
    ]
)
class PushTokens(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_token_id")
    val pushTokenId: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Column(name = "device_id", nullable = false, length = 255)
    val deviceId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false, length = 50)
    var platformType: PlatformType,

    @Column(name = "push_token", nullable = false, length = 500)
    var pushToken: String,

    @Column(name = "app_version", length = 50)
    var appVersion: String? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: OffsetDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) : BaseUpdatedTimeEntity() {

    fun updateToken(token: String, platform: PlatformType, version: String?) {
        this.pushToken = token
        this.platformType = platform
        this.appVersion = version
        this.isActive = true
        this.lastUsedAt = OffsetDateTime.now()
    }

    fun markAsUsed() {
        this.lastUsedAt = OffsetDateTime.now()
    }

    fun deactivate() {
        this.isActive = false
    }

    fun activate() {
        this.isActive = true
    }
}

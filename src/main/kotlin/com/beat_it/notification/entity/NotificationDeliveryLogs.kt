package com.beat_it.notification.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import com.beat_it.notification.entity.enum.DeliveryChannel
import com.beat_it.notification.entity.enum.DeliveryStatus
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "notification_delivery_logs",
    indexes = [
        Index(name = "idx_delivery_logs_notification_id", columnList = "notification_id"),
        Index(name = "idx_delivery_logs_public_id", columnList = "public_id")
    ]
)
class NotificationDeliveryLogs(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_delivery_log_id")
    val notificationDeliveryLogId: Long? = null,

    @Column(name = "public_id", nullable = false, unique = true)
    val publicId: UUID = UUID.randomUUID(),

    @Column(name = "notification_id", nullable = false)
    val notificationId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", nullable = false, length = 20)
    val deliveryChannel: DeliveryChannel,

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false, length = 20)
    var deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,

    @Column(name = "delivered_at")
    var deliveredAt: OffsetDateTime? = null,

    @Column(name = "failure_reason", length = 255)
    var failureReason: String? = null,
) : BaseCreatedTimeEntity() {

    fun markSuccess() {
        this.deliveryStatus = DeliveryStatus.SUCCESS
        this.deliveredAt = OffsetDateTime.now()
        this.failureReason = null
    }

    fun markFailed(reason: String?) {
        this.deliveryStatus = DeliveryStatus.FAILED
        this.deliveredAt = OffsetDateTime.now()
        this.failureReason = reason?.take(255)
    }
}

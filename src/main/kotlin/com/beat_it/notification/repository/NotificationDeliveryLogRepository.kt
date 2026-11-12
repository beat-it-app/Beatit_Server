package com.beat_it.notification.repository

import com.beat_it.notification.entity.NotificationDeliveryLogs
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationDeliveryLogRepository : JpaRepository<NotificationDeliveryLogs, Long> {

    fun findAllByNotificationId(notificationId: Long): List<NotificationDeliveryLogs>

    fun findByPublicId(publicId: UUID): NotificationDeliveryLogs?
}

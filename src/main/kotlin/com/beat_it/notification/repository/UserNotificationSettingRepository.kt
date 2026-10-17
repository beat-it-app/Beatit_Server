package com.beat_it.notification.repository

import com.beat_it.notification.entity.UserNotificationSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserNotificationSettingRepository : JpaRepository<UserNotificationSettings, Long> {
    fun findByUserId(userId: Long): UserNotificationSettings?
    fun findAllByUserIdIn(userIds: List<Long>): List<UserNotificationSettings>
}

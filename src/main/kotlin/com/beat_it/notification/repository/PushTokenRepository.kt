package com.beat_it.notification.repository

import com.beat_it.notification.entity.PushTokens
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PushTokenRepository : JpaRepository<PushTokens, Long> {

    fun findByDeviceIdAndUserId(deviceId: String, userId: Long): PushTokens?

    fun findAllByUserIdAndIsActiveTrue(userId: Long): List<PushTokens>

    fun findAllByUserIdInAndIsActiveTrue(userIds: List<Long>): List<PushTokens>

    fun findByPushToken(pushToken: String): PushTokens?

    fun deleteByDeviceIdAndUserId(deviceId: String, userId: Long)
}

package com.beat_it.notification.dto

import com.beat_it.notification.template.NotificationMessage

data class NotificationKafkaDto(
    val targetUserIds: List<Long>,
    val teamId: Long,
    val message: NotificationMessage
) {
    constructor(targetUserId: Long, teamId: Long, message: NotificationMessage) : this(
        targetUserIds = listOf(targetUserId),
        teamId = teamId,
        message = message
    )
}

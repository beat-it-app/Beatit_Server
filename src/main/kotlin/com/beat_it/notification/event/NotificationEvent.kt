package com.beat_it.notification.event

import com.beat_it.notification.template.NotificationMessage

data class NotificationEvent(
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

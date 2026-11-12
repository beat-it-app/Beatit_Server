package com.beat_it.team.dto

data class TeamCloudItemsMoveRequest(
    val itemIds: List<Long>,
    val targetFolderId: Long?
)
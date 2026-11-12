package com.beat_it.team.dto

data class TeamCloudFileDetailResponse(
    val itemId: Long,
    val itemName: String,
    val fileSize: Long,
    val mimeType: String,
    val fileUrl: String
)
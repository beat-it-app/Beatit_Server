package com.beat_it.team.dto

import java.time.OffsetDateTime

data class ArchiveDetailResponse(
    val archiveId: Long,
    val title: String,
    val placeName: String?,
    val description: String?,
    val archiveImageUrl: String?,
//    val location: LocationResponse?,
    val likeCount: Int,
    val dislikeCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

data class LocationResponse(
    val locationId: Long,
    val locationName: String,
    val roadAddress: String,
    val latitude: String,
    val longitude: String,
)
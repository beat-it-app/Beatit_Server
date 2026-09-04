package com.beat_it.team.dto

data class ArchiveRatingResponse(
    val averageRating: Double,
    val ratingCount: Int,
    val myRating: Int?,
)

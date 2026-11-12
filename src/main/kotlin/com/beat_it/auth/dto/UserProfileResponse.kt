package com.beat_it.auth.dto

data class UserProfileResponse(
    val userId: Long,
    val name: String,
    val profileImageUrl: String,
)

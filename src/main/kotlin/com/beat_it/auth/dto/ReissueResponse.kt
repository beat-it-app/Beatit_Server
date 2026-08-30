package com.beat_it.auth.dto

data class ReissueResponse(
    val accessToken: String,
    val refreshToken: String
)

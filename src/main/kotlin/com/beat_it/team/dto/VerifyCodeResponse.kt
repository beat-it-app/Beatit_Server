package com.beat_it.team.dto

data class VerifyCodeResponse(
    val teamId: Long,
    val teamName: String,
    val inviteCode: String? = null
)
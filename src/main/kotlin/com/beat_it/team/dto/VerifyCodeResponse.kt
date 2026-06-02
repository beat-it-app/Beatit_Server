package com.beat_it.team.dto

import java.util.UUID

data class VerifyCodeResponse(
    val teamId: Long,
    val teamPublicId: UUID,
    val teamName: String,
    val inviteCode: String? = null
)
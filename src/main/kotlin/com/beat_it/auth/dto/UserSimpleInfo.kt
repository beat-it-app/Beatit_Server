package com.beat_it.auth.dto

import com.beat_it.team.entity.enum.TeamRole
import java.util.UUID

data class UserSimpleInfo(
    val userId: Long,
    val userPublicId: UUID,
    val userName: String,
    val profileImageUrl: String?,
)


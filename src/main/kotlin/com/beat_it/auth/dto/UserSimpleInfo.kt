package com.beat_it.auth.dto

import java.util.UUID

data class UserSimpleInfo(
    val userPublicId: UUID,
    val userName: String,
    val profileImageUrl: String?,
)


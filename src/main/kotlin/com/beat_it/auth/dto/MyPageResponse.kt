package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.SocialProvider
import com.beat_it.team.entity.enum.TeamType
import java.time.LocalDate
import java.util.UUID

data class MyPageResponse (
    val userId: Long,
    val userName: String,
    val email: String,
    val profileImageUrl: String,
    val socialAccounts: List<SocialProvider>,
    val team: MyPageTeamResponse?,
    val unreadNotificationCount: Long,
)

data class MyPageTeamResponse(
    val teamId: Long,
    val teamPublicId: UUID,
    val type: TeamType,
    val name: String,
    val imageUrl: String,
    val leaderName: String,
    val memberCount: Int,
    val establishedOn: LocalDate? = null,
)
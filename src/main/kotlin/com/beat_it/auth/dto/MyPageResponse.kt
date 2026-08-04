package com.beat_it.auth.dto

import com.beat_it.auth.entity.enum.SocialProvider
import com.beat_it.team.entity.enum.TeamType

data class MyPageResponse (
    val userId: Long,
    val userName: String,
    val email: String,
    val profileImageUrl: String,
    val socialAccounts: List<SocialProvider>,
    val teams: List<MyPageTeamResponse>
)

data class MyPageTeamResponse(
    val type: TeamType,
    val name: String,
    val imageUrl: String,
    val leaderName: String,
    val memberCount: Int,
)
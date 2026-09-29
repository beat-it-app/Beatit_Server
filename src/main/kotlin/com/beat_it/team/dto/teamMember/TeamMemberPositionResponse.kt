package com.beat_it.team.dto.teamMember

data class TeamMemberPositionResponse(
    val members: List<MemberPositionItem>,
)

data class MemberPositionItem(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
    val position: String?,
)

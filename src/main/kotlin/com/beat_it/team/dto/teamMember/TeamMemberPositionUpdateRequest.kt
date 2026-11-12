package com.beat_it.team.dto.teamMember

data class TeamMemberPositionUpdateRequest(
    val positions: List<MemberPositionUpdateItem>,
)

data class MemberPositionUpdateItem(
    val userId: Long,
    val position: String?,
)

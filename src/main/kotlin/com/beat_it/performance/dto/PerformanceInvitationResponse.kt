package com.beat_it.performance.dto

import com.beat_it.performance.entity.InvitationCard
import com.beat_it.performance.entity.Performances
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "모바일 초대장 조회 응답 DTO")
data class PerformanceInvitationResponse(
    val title: String,
    val performanceTeam: String? = null,
    val performanceDateTime: OffsetDateTime,
    val placeName: String?,

    val invitationDescription: String? = null,

    val setlist: List<SetlistTeamDto>? = null
) {
    companion object {
        fun from(
            performance: Performances,
            invitationCard: InvitationCard? = null,
            setlist: List<SetlistTeamDto>? = null
        ): PerformanceInvitationResponse {
            return PerformanceInvitationResponse(
                title = performance.title,
                performanceTeam = invitationCard?.performanceTeam,
                performanceDateTime = performance.performanceDateTime,
                placeName = performance.placeName,
                invitationDescription = invitationCard?.description,
                setlist = setlist
            )
        }
    }
}

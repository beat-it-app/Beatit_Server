package com.beat_it.performance.dto

import com.beat_it.performance.entity.Performances
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "모바일 초대장 조회 응답 DTO")
data class PerformanceInvitationResponse(
    @Schema(description = "팀 이름", example = "비트잇 밴드")
    val teamName: String?,

    @Schema(description = "공연 제목")
    val title: String,

    @Schema(description = "공연 일시")
    val performanceDateTime: OffsetDateTime,

    @Schema(description = "공연 장소명")
    val placeName: String?,

    @Schema(description = "공연 상세 설명")
    val description: String?
) {
    companion object {
        fun from(performance: Performances, teamName: String? = null): PerformanceInvitationResponse {
            return PerformanceInvitationResponse(
                teamName = teamName,
                title = performance.title,
                performanceDateTime = performance.performanceDateTime,
                placeName = performance.placeName,
                description = performance.description
            )
        }
    }
}

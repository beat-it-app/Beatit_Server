package com.beat_it.performance.dto

import com.beat_it.performance.entity.enum.PerformanceStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공연 상태 변경 요청 DTO")
data class PerformanceStatusUpdateRequest(
    @Schema(description = "변경할 공연 상태 (PUBLISHED: 공개, CLOSED: 비공개/마감)", example = "CLOSED")
    val status: PerformanceStatus
)

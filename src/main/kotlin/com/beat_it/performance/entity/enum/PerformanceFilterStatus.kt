package com.beat_it.performance.entity.enum

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공연 목록 필터 상태 (ALL: 전체, UPCOMING: 진행예정만, PAST: 지나간 공연만)")
enum class PerformanceFilterStatus(val description: String) {
    ALL("진행예정 + 지나간 공연 전체"),
    UPCOMING("진행예정 공연만"),
    PAST("지나간 공연만")
}

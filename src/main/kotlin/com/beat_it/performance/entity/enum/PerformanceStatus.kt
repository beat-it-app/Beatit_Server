package com.beat_it.performance.entity.enum

enum class PerformanceStatus(val description: String) {
    UPCOMING("진행예정 공연"),
    PAST("지나간 공연"),

    // 기존 데이터베이스 레코드 하위 호환
    PUBLISHED("진행예정 공연"),
    CLOSED("지나간 공연")
}

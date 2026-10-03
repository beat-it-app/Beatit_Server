package com.beat_it.team.repository

import com.beat_it.performance.entity.Performances
import org.springframework.data.repository.Repository
import java.time.OffsetDateTime

interface TeamPerformanceQueryRepository : Repository<Performances, Long> {

    fun findTop10ByTeamIdAndPerformanceDateTimeGreaterThanEqualOrderByPerformanceDateTimeAsc(
        teamId: Long,
        performanceDateTime: OffsetDateTime,
    ): List<Performances>
}

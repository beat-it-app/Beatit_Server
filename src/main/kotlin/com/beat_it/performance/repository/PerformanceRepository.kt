package com.beat_it.performance.repository

import com.beat_it.performance.entity.Performances
import com.beat_it.performance.entity.enum.PerformanceStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface PerformanceRepository : JpaRepository<Performances, Long> {
    fun findByPublicId(publicId: UUID): Optional<Performances>

    fun findByTeamId(teamId: Long, pageable: Pageable): Page<Performances>

    fun findByTeamIdAndPerformanceStatus(
        teamId: Long,
        performanceStatus: PerformanceStatus,
        pageable: Pageable
    ): Page<Performances>

    fun findByPerformanceStatus(
        performanceStatus: PerformanceStatus,
        pageable: Pageable
    ): Page<Performances>
}

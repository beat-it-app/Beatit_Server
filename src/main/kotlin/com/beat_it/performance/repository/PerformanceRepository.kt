package com.beat_it.performance.repository

import com.beat_it.performance.entity.Performances
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface PerformanceRepository : JpaRepository<Performances, Long> {
    fun findByPublicId(publicId: UUID): Optional<Performances>

    @Query("""
        SELECT p FROM Performances p 
        WHERE p.teamId = :teamId 
        AND (COALESCE(:keyword, '') = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (
            :filterType = 'ALL' 
            OR (:filterType = 'UPCOMING' AND (p.performanceStatus = com.beat_it.performance.entity.enum.PerformanceStatus.UPCOMING OR p.performanceDateTime >= :now))
            OR (:filterType = 'PAST' AND (p.performanceStatus = com.beat_it.performance.entity.enum.PerformanceStatus.PAST OR (p.performanceStatus != com.beat_it.performance.entity.enum.PerformanceStatus.UPCOMING AND p.performanceDateTime < :now)))
        )
    """)
    fun searchMyPerformances(
        @Param("teamId") teamId: Long,
        @Param("keyword") keyword: String?,
        @Param("filterType") filterType: String,
        @Param("now") now: OffsetDateTime,
        pageable: Pageable
    ): Page<Performances>

    @Query("""
        SELECT p FROM Performances p 
        WHERE (COALESCE(:keyword, '') = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (
            :filterType = 'ALL' 
            OR (:filterType = 'UPCOMING' AND (p.performanceStatus = com.beat_it.performance.entity.enum.PerformanceStatus.UPCOMING OR p.performanceDateTime >= :now))
            OR (:filterType = 'PAST' AND (p.performanceStatus = com.beat_it.performance.entity.enum.PerformanceStatus.PAST OR (p.performanceStatus != com.beat_it.performance.entity.enum.PerformanceStatus.UPCOMING AND p.performanceDateTime < :now)))
        )
    """)
    fun searchAllPerformances(
        @Param("keyword") keyword: String?,
        @Param("filterType") filterType: String,
        @Param("now") now: OffsetDateTime,
        pageable: Pageable
    ): Page<Performances>

    @Modifying
    @Query("""
        UPDATE Performances p 
        SET p.performanceStatus = com.beat_it.performance.entity.enum.PerformanceStatus.PAST 
        WHERE p.performanceStatus = com.beat_it.performance.entity.enum.PerformanceStatus.UPCOMING 
        AND p.performanceDateTime < :now
    """)
    fun updateExpiredPerformancesToPast(@Param("now") now: OffsetDateTime): Int
}

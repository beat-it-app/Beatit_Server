package com.beat_it.cal.repository

import com.beat_it.cal.entity.Schedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface ScheduleRepository : JpaRepository<Schedule, Long> {
    @Query("""
        SELECT s FROM Schedule s
        WHERE s.teamId = :teamId
          AND s.startsAt <= :endDateTime
          AND s.endsAt >= :startDateTime
        ORDER BY s.startsAt ASC
    """)
    fun findByTeamIdAndMonthRange(
        @Param("teamId") teamId: Long,
        @Param("startDateTime") startDateTime: OffsetDateTime,
        @Param("endDateTime") endDateTime: OffsetDateTime
    ): List<Schedule>

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.teamId = :teamId
          AND s.startsAt <= :endDateTime
          AND s.endsAt >= :startDateTime
        ORDER BY s.startsAt ASC
    """)
    fun findByTeamIdAndDailyRange(
        @Param("teamId") teamId: Long,
        @Param("startDateTime") startDateTime: OffsetDateTime,
        @Param("endDateTime") endDateTime: OffsetDateTime
    ): List<Schedule>

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.teamId = :teamId
          AND s.startsAt <= :endAt
          AND s.endsAt >= :startAt
        ORDER BY s.startsAt ASC
    """)
    fun findUpcomingByTeamIdAndRange(
        @Param("teamId") teamId: Long,
        @Param("startAt") startAt: OffsetDateTime,
        @Param("endAt") endAt: OffsetDateTime
    ): List<Schedule>

    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.startsAt >= :startDateTime 
          AND s.startsAt < :endDateTime
        ORDER BY s.startsAt ASC
    """)
    fun findByStartsAtBetween(
        @Param("startDateTime") startDateTime: OffsetDateTime,
        @Param("endDateTime") endDateTime: OffsetDateTime
    ): List<Schedule>

    @Query("""
        SELECT COUNT(DISTINCT s.scheduleId) FROM Schedule s
        LEFT JOIN s.participants p
        WHERE s.userId = :userId OR p.userId = :userId
    """)
    fun countSchedulesByUserId(@Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(DISTINCT s.scheduleId) FROM Schedule s
        LEFT JOIN s.participants p
        WHERE s.teamId = :teamId AND (s.userId = :userId OR p.userId = :userId)
    """)
    fun countSchedulesByTeamIdAndUserId(@Param("teamId") teamId: Long, @Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(s) FROM Schedule s
        WHERE s.teamId = :teamId
    """)
    fun countSchedulesByTeamId(@Param("teamId") teamId: Long): Long
}
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
        WHERE s.userId = :userId 
          AND s.startsAt <= :endDateTime 
          AND s.endsAt >= :startDateTime
        ORDER BY s.startsAt ASC
    """)
    fun findByUserIdAndMonthRange(
        @Param("userId") userId: Long,
        @Param("startDateTime") startDateTime: OffsetDateTime,
        @Param("endDateTime") endDateTime: OffsetDateTime
    ): List<Schedule>

    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.userId = :userId 
          AND s.startsAt <= :endDateTime 
          AND s.endsAt >= :startDateTime
        ORDER BY s.startsAt ASC
    """)
    fun findByUserIdAndDailyRange(
        @Param("userId") userId: Long,
        @Param("startDateTime") startDateTime: OffsetDateTime,
        @Param("endDateTime") endDateTime: OffsetDateTime
    ): List<Schedule>
}
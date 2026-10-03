package com.beat_it.team.repository

import com.beat_it.cal.entity.Schedule
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface TeamCalendarQueryRepository : Repository<Schedule, Long> {

    @Query(
        """
        SELECT s FROM Schedule s
        WHERE s.teamId = :teamId
          AND s.startsAt >= :startAt
          AND s.startsAt < :endAt
        ORDER BY s.startsAt ASC
        """
    )
    fun findAllByTeamIdWithinRange(
        @Param("teamId") teamId: Long,
        @Param("startAt") startAt: OffsetDateTime,
        @Param("endAt") endAt: OffsetDateTime,
    ): List<Schedule>
}

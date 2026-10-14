package com.beat_it.cal.repository

import com.beat_it.cal.entity.ScheduleParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleParticipantRepository : JpaRepository<ScheduleParticipant, Long> {
    fun findAllByScheduleScheduleIdIn(scheduleIds: List<Long>): List<ScheduleParticipant>
}
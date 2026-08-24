package com.beat_it.post.repository.meetit

import com.beat_it.post.entity.meetit.MeetitResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface MeetitResponseRepository : JpaRepository<MeetitResponse, Long> {
    fun findByMeetitMeetitId(meetitId: Long): List<MeetitResponse>

    @Modifying
    fun deleteByMeetitParticipantMeetitParticipantId(meetitParticipantId: Long)
}

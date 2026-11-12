package com.beat_it.post.repository.meetit

import com.beat_it.post.entity.meetit.MeetitParticipant
import org.springframework.data.jpa.repository.JpaRepository

interface MeetitParticipantRepository : JpaRepository<MeetitParticipant, Long> {
    fun findByMeetitMeetitIdAndUserId(meetitId: Long, userId: Long): MeetitParticipant?
    fun findByMeetitMeetitId(meetitId: Long): List<MeetitParticipant>
}

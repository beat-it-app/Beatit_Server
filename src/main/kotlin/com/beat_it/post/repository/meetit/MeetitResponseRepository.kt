package com.beat_it.post.repository.meetit

import com.beat_it.post.entity.meetit.MeetitResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MeetitResponseRepository : JpaRepository<MeetitResponse, Long> {
    fun findByMeetitMeetitId(meetitId: Long): List<MeetitResponse>

    @Modifying
    @Query("DELETE FROM MeetitResponse r WHERE r.meetitParticipant.meetitParticipantId = :meetitParticipantId")
    fun deleteByMeetitParticipantMeetitParticipantId(@Param("meetitParticipantId") meetitParticipantId: Long)

    @Modifying
    @Query("DELETE FROM MeetitResponse r WHERE r.meetit.meetitId = :meetitId")
    fun deleteByMeetitMeetitId(@Param("meetitId") meetitId: Long)
}

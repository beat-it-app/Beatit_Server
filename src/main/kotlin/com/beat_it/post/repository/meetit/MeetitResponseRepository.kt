package com.beat_it.post.repository.meetit

import com.beat_it.post.entity.meetit.MeetitResponse
import org.springframework.data.jpa.repository.JpaRepository

interface MeetitResponseRepository : JpaRepository<MeetitResponse, Long>

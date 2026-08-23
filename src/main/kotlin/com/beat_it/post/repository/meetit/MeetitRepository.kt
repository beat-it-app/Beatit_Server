package com.beat_it.post.repository.meetit

import com.beat_it.post.entity.meetit.Meetit
import org.springframework.data.jpa.repository.JpaRepository

interface MeetitRepository : JpaRepository<Meetit, Long>

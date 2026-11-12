package com.beat_it.post.repository.meetit

import com.beat_it.post.entity.meetit.Meetit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MeetitRepository : JpaRepository<Meetit, Long> {
    fun findByTeamId(teamId: Long, pageable: Pageable): Page<Meetit>
}

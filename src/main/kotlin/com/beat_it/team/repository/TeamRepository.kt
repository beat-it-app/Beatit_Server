package com.beat_it.team.repository

import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeamRepository : JpaRepository<Teams, Long> {
    fun findByTeamId(teamId: Long): Teams?

    fun findByPublicId(publicId: UUID): Teams?

    fun findByInviteCode(inviteCode: String): Teams?

    fun existsByInviteCode(inviteCode: String): Boolean
}
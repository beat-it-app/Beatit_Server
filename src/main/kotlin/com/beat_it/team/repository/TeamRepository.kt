package com.beat_it.team.repository

import com.beat_it.team.entity.TeamMemberships
import com.beat_it.team.entity.Teams
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TeamRepository : JpaRepository<Teams, Long> {

    fun findByPublicId(publicId: UUID): Teams?

    fun findByPublicIdAndDeletedAtIsNull(publicId: UUID): Teams?

    fun findByInviteCodeAndDeletedAtIsNull(inviteCode: String): Teams?

    fun existsByInviteCode(inviteCode: String): Boolean
}
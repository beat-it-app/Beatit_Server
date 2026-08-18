package com.beat_it.team.repository

import com.beat_it.team.entity.TeamFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamFileRepository : JpaRepository<TeamFile, Long> {
}
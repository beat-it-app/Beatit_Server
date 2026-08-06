package com.beat_it.team.repository

import com.beat_it.team.entity.TeamFiles
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamFilesRepository : JpaRepository<TeamFiles, Long> {
}
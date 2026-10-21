package com.beat_it.cal.repository

import com.beat_it.cal.entity.ScheduleFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ScheduleFileRepository : JpaRepository<ScheduleFile, Long> {

    @Query("""
        SELECT COUNT(sf) FROM ScheduleFile sf
        JOIN sf.schedule s
        WHERE s.userId = :userId
    """)
    fun countByUserId(@Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(sf) FROM ScheduleFile sf
        JOIN sf.schedule s
        WHERE s.teamId = :teamId AND s.userId = :userId
    """)
    fun countByTeamIdAndUserId(@Param("teamId") teamId: Long, @Param("userId") userId: Long): Long

    @Query("""
        SELECT COUNT(sf) FROM ScheduleFile sf
        JOIN sf.schedule s
        WHERE s.teamId = :teamId
    """)
    fun countByTeamId(@Param("teamId") teamId: Long): Long
}

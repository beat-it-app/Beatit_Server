package com.beat_it.post.repository

import com.beat_it.post.entity.Polls
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PollRepository : JpaRepository<Polls, Long> {

    @Query("SELECT p FROM Polls p WHERE p.teamId = :teamId")
    fun getPolls(
        @Param("teamId") teamId: Long,
        pageable: Pageable
    ): Page<Polls>

    @Query("SELECT v.poll.pollId FROM PollVotes v WHERE v.userId = :userId AND v.poll.pollId IN :pollIds")
    fun findVotedPollIdsByUserIdAndPollIds(
        @Param("userId") userId: Long,
        @Param("pollIds") pollIds: List<Long>
    ): List<Long>
}
package com.beat_it.post.repository

import com.beat_it.post.entity.Polls // 실제 엔티티 패키지 경로에 맞게 지정
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PollRepository : JpaRepository<Polls, Long> {

    @Query("SELECT p FROM Polls p WHERE p.teamId = :teamId")
    fun getPolls(
        @Param("teamId") teamId: Long,
        pageable: Pageable
    ): List<Polls>

    @Query("SELECT v.poll.pollId FROM PollVotes v WHERE v.userId = :userId AND v.poll.pollId IN :pollIds")
    fun findVotedPollIdsByUserIdAndPollIds(
        @Param("userId") userId: Long,
        @Param("pollIds") pollIds: List<Long>
    ): List<Long>
}
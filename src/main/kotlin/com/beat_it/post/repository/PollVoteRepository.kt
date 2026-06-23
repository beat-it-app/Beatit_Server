package com.beat_it.post.repository

import com.beat_it.post.entity.PollVotes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PollVoteRepository : JpaRepository<PollVotes, Long> {

    // 1. 현재 로그인한 유저가 이 투표(pollId) 내에서 선택한 옵션 ID(pollOptionId) 목록을 조회
    @Query("SELECT pv.pollOption.pollOptionId FROM PollVotes pv WHERE pv.userId = :userId AND pv.poll.pollId = :pollId")
    fun findVotedOptionIdsByUserIdAndPollId(
        @Param("userId") userId: Long,
        @Param("pollId") pollId: Long
    ): List<Long>

    // 2. 이 투표(pollId) 내의 모든 선택지별 투표 개수를 집계 (OptionId -> Count)
    // 만약 PollOptions 엔티티 내부나 별도 캐시 테이블에 voteCount를 관리하고 있지 않다면, 이 쿼리로 한 번에 집계하는 것이 효율적입니다.
    @Query("SELECT pv.pollOption.pollOptionId, COUNT(pv) FROM PollVotes pv WHERE pv.poll.pollId = :pollId GROUP BY pv.pollOption.pollOptionId")
    fun countVotesByPollId(@Param("pollId") pollId: Long): List<Array<Any>>
}